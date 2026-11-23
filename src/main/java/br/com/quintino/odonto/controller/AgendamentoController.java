package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.config.SessaoUsuario;
import br.com.quintino.odonto.dto.AgendamentoForm;
import br.com.quintino.odonto.dto.ConsultaDetalheDTO;
import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.dto.ProcedimentoDTO;
import br.com.quintino.odonto.service.AgendaService;
import br.com.quintino.odonto.service.ColaboradorService;
import br.com.quintino.odonto.service.PacienteService;
import br.com.quintino.odonto.service.ProcedimentoService;
import br.com.quintino.odonto.util.Fmt;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * Agendamento de consultas (RF12 a RF15, RF17). Quem cria, altera e cancela é o consultório
 * (regra no AutenticacaoInterceptor); o detalhe e "concluir" valem para todos os perfis logados.
 */
@Controller
@RequestMapping("/consultas")
public class AgendamentoController {

    private final AgendaService agenda;
    private final PacienteService pacientes;
    private final ProcedimentoService procedimentos;
    private final ColaboradorService colaboradores;

    public AgendamentoController(AgendaService agenda, PacienteService pacientes,
                                 ProcedimentoService procedimentos, ColaboradorService colaboradores) {
        this.agenda = agenda;
        this.pacientes = pacientes;
        this.procedimentos = procedimentos;
        this.colaboradores = colaboradores;
    }

    private ConsultaDetalheDTO obter(Long id) {
        return agenda.buscarConsulta(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /** Dentista só abre e conclui as próprias consultas (RF19); o consultório vê todas. */
    private ConsultaDetalheDTO obterDoUsuario(Long id, HttpSession sessao) {
        ConsultaDetalheDTO c = obter(id);
        SessaoUsuario usuario = (SessaoUsuario) sessao.getAttribute(SessaoUsuario.CHAVE);
        if (usuario != null && usuario.isDentista() && !usuario.getId().equals(c.dentistaId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return c;
    }

    // ---------- formulário (novo e alteração) ----------
    private void paginaForm(Model m, Map<String, String> v, String acao, String titulo, String voltar,
                            Long consultaId) {
        List<ProcedimentoDTO> todos = new ArrayList<>();
        for (ItemListaDTO item : procedimentos.buscar("")) {
            procedimentos.buscarPorId(item.id()).ifPresent(todos::add);
        }
        List<ItemListaDTO> dentistas = colaboradores.dentistas();
        m.addAttribute("v", v);
        m.addAttribute("acao", acao);
        m.addAttribute("tituloPagina", titulo);
        m.addAttribute("voltar", voltar);
        m.addAttribute("consultaId", consultaId);
        m.addAttribute("hoje", LocalDate.now().toString());
        m.addAttribute("listaPacientes", pacientes.buscar(""));
        m.addAttribute("listaProcedimentos", todos);
        m.addAttribute("listaDentistas", dentistas);
    }

    @GetMapping("/nova")
    public String nova(@RequestParam(name = "pacienteId", required = false) Long pacienteId,
                       @RequestParam(name = "data", required = false) String data, Model m) {
        Map<String, String> v = new HashMap<>();
        if (pacienteId != null) {
            v.put("pacienteId", String.valueOf(pacienteId));
        }
        if (data != null && Validacao.data(data) != null) {
            v.put("data", data);
        }
        paginaForm(m, v, "/consultas", "Novo agendamento", "/inicio", null);
        return "consulta-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        ConsultaDetalheDTO c = obter(id);
        if (!c.agendada()) {
            ra.addFlashAttribute("erros", List.of("Só é possível alterar consultas que estão agendadas."));
            return "redirect:/consultas/" + id;
        }
        Map<String, String> v = new HashMap<>();
        v.put("pacienteId", String.valueOf(c.pacienteId()));
        v.put("procedimentoId", String.valueOf(c.procedimentoId()));
        v.put("dentistaId", String.valueOf(c.dentistaId()));
        v.put("data", c.data().toString());
        v.put("hora", c.hora().toString().substring(0, 5));
        v.put("sintomas", c.sintomas());
        v.put("dente", c.dente());
        paginaForm(m, v, "/consultas/" + id, "Alterar agendamento", "/consultas/" + id, id);
        return "consulta-form";
    }

    @PostMapping
    public String criar(@RequestParam Map<String, String> v, Model m, RedirectAttributes ra) {
        return salvar(null, v, m, ra);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @RequestParam Map<String, String> v, Model m,
                            RedirectAttributes ra) {
        obter(id);
        return salvar(id, v, m, ra);
    }

    private String salvar(Long id, Map<String, String> v, Model m, RedirectAttributes ra) {
        List<String> erros = new ArrayList<>();
        Long pacienteId = numero(v, "pacienteId");
        Long procedimentoId = numero(v, "procedimentoId");
        Long dentistaId = numero(v, "dentistaId");
        LocalDate data = Validacao.data(Validacao.texto(v, "data"));
        LocalTime hora = hora(Validacao.texto(v, "hora"));
        if (pacienteId == null) {
            erros.add("Selecione o paciente.");
        }
        if (procedimentoId == null) {
            erros.add("Selecione o procedimento.");
        }
        if (dentistaId == null) {
            erros.add("Selecione o dentista.");
        }
        if (data == null) {
            erros.add("Informe a data da consulta.");
        }
        if (hora == null) {
            erros.add("Escolha um horário disponível.");
        }
        if (erros.isEmpty()) {
            try {
                AgendamentoForm form = new AgendamentoForm(pacienteId, dentistaId, procedimentoId, data, hora,
                        Validacao.texto(v, "sintomas"), Validacao.texto(v, "dente"));
                ConsultaDetalheDTO salva = id == null ? agenda.agendar(form) : agenda.alterar(id, form);
                ra.addFlashAttribute("sucesso",
                        id == null ? "Consulta agendada com sucesso." : "Consulta alterada com sucesso.");
                return "redirect:/consultas/" + salva.id();
            } catch (IllegalArgumentException e) {
                erros.add(e.getMessage());
            }
        }
        m.addAttribute("erros", erros);
        paginaForm(m, v, id == null ? "/consultas" : "/consultas/" + id,
                id == null ? "Novo agendamento" : "Alterar agendamento",
                id == null ? "/inicio" : "/consultas/" + id, id);
        return "consulta-form";
    }

    private static Long numero(Map<String, String> v, String campo) {
        try {
            return Long.valueOf(Validacao.texto(v, campo));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalTime hora(String texto) {
        try {
            return LocalTime.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ---------- detalhe, cancelar, concluir ----------
    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, HttpSession sessao, Model m) {
        m.addAttribute("consulta", obterDoUsuario(id, sessao));
        return "consulta-detalhe";
    }

    // ---------- reagendar (RF14): tela própria com os horários livres em botões ----------
    private void paginaReagendar(Model m, ConsultaDetalheDTO c, String dataNova, String horaEscolhida) {
        m.addAttribute("consulta", c);
        m.addAttribute("voltar", "/consultas/" + c.id());
        m.addAttribute("dataNova", dataNova);
        m.addAttribute("horaEscolhida", horaEscolhida);
        m.addAttribute("hoje", LocalDate.now().toString());
    }

    @GetMapping("/{id}/reagendar")
    public String reagendar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        ConsultaDetalheDTO c = obter(id);
        if (!c.agendada()) {
            ra.addFlashAttribute("erros", List.of("Só é possível reagendar consultas que estão agendadas."));
            return "redirect:/consultas/" + id;
        }
        paginaReagendar(m, c, c.data().toString(), "");
        return "consulta-reagendar";
    }

    @PostMapping("/{id}/reagendar")
    public String reagendarSalvar(@PathVariable Long id, @RequestParam Map<String, String> v, Model m,
                                  RedirectAttributes ra) {
        ConsultaDetalheDTO c = obter(id);
        List<String> erros = new ArrayList<>();
        LocalDate data = Validacao.data(Validacao.texto(v, "data"));
        LocalTime hora = hora(Validacao.texto(v, "hora"));
        if (data == null) {
            erros.add("Informe a nova data.");
        }
        if (hora == null) {
            erros.add("Escolha um dos horários disponíveis.");
        }
        if (erros.isEmpty()) {
            try {
                AgendamentoForm form = new AgendamentoForm(c.pacienteId(), c.dentistaId(), c.procedimentoId(),
                        data, hora, c.sintomas(), c.dente());
                agenda.alterar(id, form);
                ra.addFlashAttribute("sucesso",
                        "Consulta reagendada para " + Fmt.I.data(data) + " às " + Fmt.I.hora(hora) + ".");
                return "redirect:/consultas/" + id;
            } catch (IllegalArgumentException e) {
                erros.add(e.getMessage());
            }
        }
        m.addAttribute("erros", erros);
        paginaReagendar(m, c, data == null ? c.data().toString() : data.toString(), Validacao.texto(v, "hora"));
        return "consulta-reagendar";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @RequestParam(name = "motivo", defaultValue = "") String motivo,
                           RedirectAttributes ra) {
        obter(id);
        try {
            agenda.cancelar(id, motivo);
            ra.addFlashAttribute("sucesso", "Consulta cancelada.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erros", List.of(e.getMessage()));
        }
        return "redirect:/consultas/" + id;
    }

    @PostMapping("/{id}/concluir")
    public String concluir(@PathVariable Long id, HttpSession sessao, RedirectAttributes ra) {
        obterDoUsuario(id, sessao);
        try {
            agenda.concluir(id);
            ra.addFlashAttribute("sucesso", "Consulta marcada como realizada.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erros", List.of(e.getMessage()));
        }
        return "redirect:/consultas/" + id;
    }
}
