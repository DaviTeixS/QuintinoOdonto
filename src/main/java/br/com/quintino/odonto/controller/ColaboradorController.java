package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.dto.ColaboradorDTO;
import br.com.quintino.odonto.dto.HorarioDTO;
import br.com.quintino.odonto.service.AgendaService;
import br.com.quintino.odonto.service.ColaboradorService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Telas de colaboradores (somente consultório: administrativo e recepção - ver AutenticacaoInterceptor). */
@Controller
@RequestMapping("/colaboradores")
public class ColaboradorController {

    /** Dias da semana (valor do enum -> rótulo exibido), na ordem de segunda a domingo. */
    private static final Map<String, String> DIAS = new LinkedHashMap<>();

    static {
        DIAS.put("MONDAY", "Segunda-feira");
        DIAS.put("TUESDAY", "Terça-feira");
        DIAS.put("WEDNESDAY", "Quarta-feira");
        DIAS.put("THURSDAY", "Quinta-feira");
        DIAS.put("FRIDAY", "Sexta-feira");
        DIAS.put("SATURDAY", "Sábado");
        DIAS.put("SUNDAY", "Domingo");
    }

    private final ColaboradorService colaboradores;
    private final AgendaService agenda;

    public ColaboradorController(ColaboradorService colaboradores, AgendaService agenda) {
        this.colaboradores = colaboradores;
        this.agenda = agenda;
    }

    private ColaboradorDTO obter(Long id) {
        return colaboradores.buscarPorId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public String lista() {
        return "colaboradores";
    }

    // ---------- novo (mesmo formulário do cadastro, com a função) ----------
    @GetMapping("/novo")
    public String novo(Model m) {
        paginaNovo(m, Map.of());
        return "cadastro";
    }

    private void paginaNovo(Model m, Map<String, String> v) {
        m.addAttribute("v", v);
        m.addAttribute("acao", "/colaboradores");
        m.addAttribute("tituloPagina", "Novo colaborador");
        m.addAttribute("voltar", "/colaboradores");
        m.addAttribute("mostrarFuncao", true);
        m.addAttribute("hoje", LocalDate.now().toString());
    }

    @PostMapping
    public String criar(@RequestParam Map<String, String> v, Model m, RedirectAttributes ra) {
        List<String> erros = new ArrayList<>();
        CadastroValidador.validar(v, true, erros);
        if (erros.isEmpty()) {
            try {
                colaboradores.cadastrar(CadastroValidador.montar(v));
                ra.addFlashAttribute("sucesso", "Colaborador cadastrado com sucesso.");
                return "redirect:/colaboradores";
            } catch (IllegalArgumentException e) {
                erros.add(e.getMessage());
            }
        }
        m.addAttribute("erros", erros);
        paginaNovo(m, CadastroValidador.semSenhas(v));
        return "cadastro";
    }

    // ---------- editar ----------
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m) {
        ColaboradorDTO c = obter(id);
        Map<String, String> v = new HashMap<>();
        v.put("nome", c.nome());
        v.put("funcao", c.funcao());
        v.put("email", c.email());
        v.put("celular", c.celular());
        v.put("cpf", c.cpf());
        v.put("cro", c.cro());
        paginaEditar(m, v, id);
        return "colaborador-form";
    }

    private void paginaEditar(Model m, Map<String, String> v, Long id) {
        m.addAttribute("v", v);
        m.addAttribute("colaboradorId", id);
        m.addAttribute("acao", "/colaboradores/" + id);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @RequestParam Map<String, String> v, Model m,
                            RedirectAttributes ra) {
        obter(id);
        List<String> erros = new ArrayList<>();
        String nome = Validacao.texto(v, "nome");
        String funcao = Validacao.texto(v, "funcao");
        String email = Validacao.texto(v, "email");
        String celular = Validacao.texto(v, "celular");
        String cpf = Validacao.texto(v, "cpf");
        String cro = Validacao.texto(v, "cro");

        if (nome.length() < 3) {
            erros.add("Informe o nome completo.");
        }
        if (!funcao.equals("DENTISTA") && !funcao.equals("RECEPCIONISTA")) {
            erros.add("Selecione a função.");
        }
        if (!Validacao.email(email)) {
            erros.add("Informe um e-mail válido.");
        }
        if (!Validacao.telefone(celular)) {
            erros.add("Informe o celular com DDD (10 ou 11 dígitos).");
        }
        if (!Validacao.cpf(cpf)) {
            erros.add("Informe o CPF com 11 dígitos.");
        }
        if (funcao.equals("DENTISTA") && cro.isEmpty()) {
            erros.add("Informe o registro profissional (CRO) do dentista.");
        }
        if (!erros.isEmpty()) {
            m.addAttribute("erros", erros);
            paginaEditar(m, v, id);
            return "colaborador-form";
        }
        try {
            colaboradores.salvar(new ColaboradorDTO(id, nome, funcao, email.toLowerCase(), celular, cpf, cro));
        } catch (IllegalArgumentException e) {
            erros.add(e.getMessage());
            m.addAttribute("erros", erros);
            paginaEditar(m, v, id);
            return "colaborador-form";
        }
        ra.addFlashAttribute("sucesso", "Colaborador atualizado com sucesso.");
        return "redirect:/colaboradores";
    }

    // ---------- horários de atendimento do dentista (RF10) ----------

    /** Uma linha da tela: um dia da semana com seus períodos de atendimento. */
    public record LinhaDia(String chave, String nome, List<HorarioDTO> periodos) {
    }

    @GetMapping("/{id}/horarios")
    public String horarios(@PathVariable Long id, Model m, RedirectAttributes ra) {
        ColaboradorDTO c = obter(id);
        if (!"DENTISTA".equals(c.funcao())) {
            ra.addFlashAttribute("erros", List.of("Horários de atendimento existem apenas para dentistas."));
            return "redirect:/colaboradores/" + id + "/editar";
        }
        paginaHorarios(m, c, agenda.horarios(c.id()));
        return "horarios";
    }

    private void paginaHorarios(Model m, ColaboradorDTO c, List<HorarioDTO> periodos) {
        List<LinhaDia> linhas = new ArrayList<>();
        for (Map.Entry<String, String> dia : DIAS.entrySet()) {
            List<HorarioDTO> doDia = periodos.stream()
                    .filter(p -> p.diaSemana().name().equals(dia.getKey()))
                    .sorted(Comparator.comparing(HorarioDTO::inicio))
                    .toList();
            linhas.add(new LinhaDia(dia.getKey(), dia.getValue().replace("-feira", ""), doDia));
        }
        m.addAttribute("colaborador", c);
        m.addAttribute("linhas", linhas);
        m.addAttribute("listaDentistas", colaboradores.dentistas());
        m.addAttribute("voltar", "/colaboradores/" + c.id() + "/editar");
        m.addAttribute("tituloPagina", "Horários de atendimento");
    }

    /** Botão "Salvar horários": recebe todos os períodos ("MONDAY|08:00|12:00") e substitui os do dentista. */
    @PostMapping("/{id}/horarios")
    public String salvarHorarios(@PathVariable Long id,
                                 @RequestParam(name = "periodo", required = false) List<String> periodos,
                                 Model m, RedirectAttributes ra) {
        ColaboradorDTO c = obter(id);
        if (!"DENTISTA".equals(c.funcao())) {
            return "redirect:/colaboradores/" + id + "/editar";
        }
        List<HorarioDTO> novos = new ArrayList<>();
        try {
            for (String p : periodos == null ? List.<String>of() : periodos) {
                String[] partes = p.split("\\|");
                novos.add(new HorarioDTO(null, DayOfWeek.valueOf(partes[0]),
                        LocalTime.parse(partes[1]), LocalTime.parse(partes[2])));
            }
        } catch (IllegalArgumentException | java.time.DateTimeException | ArrayIndexOutOfBoundsException e) {
            m.addAttribute("erros", List.of("Informe o horário inicial e o final de todos os períodos."));
            paginaHorarios(m, c, agenda.horarios(id));
            return "horarios";
        }
        try {
            agenda.salvarHorarios(id, novos);
        } catch (IllegalArgumentException e) {
            m.addAttribute("erros", List.of(e.getMessage()));
            paginaHorarios(m, c, novos);
            return "horarios";
        }
        ra.addFlashAttribute("sucesso", "Horários de atendimento salvos.");
        return "redirect:/colaboradores/" + id + "/horarios";
    }
}
