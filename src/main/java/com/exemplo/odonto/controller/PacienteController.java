package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.config.SessaoUsuario;
import br.com.quintino.odonto.dto.PacienteDTO;
import br.com.quintino.odonto.dto.SaudeDTO;
import br.com.quintino.odonto.service.PacienteService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

/** Telas: lista de pacientes, novo/editar e todas as telas da ficha do paciente. */
@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacientes;

    public PacienteController(PacienteService pacientes) {
        this.pacientes = pacientes;
    }

    private PacienteDTO obter(Long id) {
        return pacientes.buscarPorId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Map<String, String> paraMapa(PacienteDTO p) {
        Map<String, String> v = new HashMap<>();
        v.put("nome", p.nome());
        v.put("sexo", p.sexo());
        v.put("nascimento", p.nascimento() == null ? "" : p.nascimento().toString());
        v.put("celular", p.celular());
        v.put("rg", p.rg());
        v.put("cpf", p.cpf());
        return v;
    }

    private void paginaForm(Model m, Map<String, String> v, String acao, String titulo, String voltar) {
        m.addAttribute("v", v);
        m.addAttribute("acao", acao);
        m.addAttribute("tituloPagina", titulo);
        m.addAttribute("voltar", voltar);
        m.addAttribute("hoje", LocalDate.now().toString());
    }

    // ---------- lista ----------
    @GetMapping
    public String lista() {
        return "pacientes";
    }

    // ---------- novo / editar ----------
    @GetMapping("/novo")
    public String novo(Model m) {
        paginaForm(m, Map.of(), "/pacientes", "Novo paciente", "/pacientes");
        return "paciente-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m) {
        PacienteDTO p = obter(id);
        paginaForm(m, paraMapa(p), "/pacientes/" + id, "Editar paciente", "/pacientes/" + id + "/cadastro");
        return "paciente-form";
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
        String nome = Validacao.texto(v, "nome");
        String sexo = Validacao.texto(v, "sexo");
        String celular = Validacao.texto(v, "celular");
        String rg = Validacao.texto(v, "rg");
        String cpf = Validacao.texto(v, "cpf");
        LocalDate nascimento = Validacao.data(Validacao.texto(v, "nascimento"));

        if (nome.length() < 3) {
            erros.add("Informe o nome completo do paciente.");
        }
        if (sexo.isEmpty()) {
            erros.add("Selecione o sexo.");
        }
        if (nascimento == null) {
            erros.add("Informe uma data de nascimento válida.");
        } else if (nascimento.isAfter(LocalDate.now())) {
            erros.add("A data de nascimento não pode ser futura.");
        }
        if (!Validacao.telefone(celular)) {
            erros.add("Informe o celular com DDD (10 ou 11 dígitos).");
        }
        if (!Validacao.cpf(cpf)) {
            erros.add("Informe o CPF com 11 dígitos.");
        }

        if (!erros.isEmpty()) {
            m.addAttribute("erros", erros);
            paginaForm(m, v, id == null ? "/pacientes" : "/pacientes/" + id,
                    id == null ? "Novo paciente" : "Editar paciente",
                    id == null ? "/pacientes" : "/pacientes/" + id + "/cadastro");
            return "paciente-form";
        }

        PacienteDTO salvo = pacientes.salvar(new PacienteDTO(id, nome, sexo, nascimento, celular, rg, cpf));
        ra.addFlashAttribute("sucesso",
                id == null ? "Paciente cadastrado com sucesso." : "Dados do paciente atualizados.");
        return "redirect:/pacientes/" + salvo.id();
    }

    // ---------- ficha ----------
    @GetMapping("/{id}")
    public String ficha(@PathVariable Long id, Model m) {
        m.addAttribute("paciente", obter(id));
        return "paciente-ficha";
    }

    @GetMapping("/{id}/cadastro")
    public String cadastro(@PathVariable Long id, Model m) {
        m.addAttribute("paciente", obter(id));
        return "paciente-cadastro";
    }

    @GetMapping("/{id}/consultas")
    public String consultas(@PathVariable Long id, Model m) {
        m.addAttribute("paciente", obter(id));
        m.addAttribute("consultas", pacientes.historicoConsultas(id));
        return "paciente-consultas";
    }

    @GetMapping("/{id}/tratamento")
    public String tratamento(@PathVariable Long id, Model m) {
        m.addAttribute("paciente", obter(id));
        m.addAttribute("tratamento", pacientes.tratamentoAtual(id).orElse(null));
        return "paciente-tratamento";
    }

    // ---------- saúde ----------
    @GetMapping("/{id}/saude")
    public String saude(@PathVariable Long id, Model m) {
        m.addAttribute("paciente", obter(id));
        m.addAttribute("saude", pacientes.saude(id));
        return "paciente-saude";
    }

    @GetMapping("/{id}/saude/editar")
    public String saudeEditar(@PathVariable Long id, Model m) {
        m.addAttribute("paciente", obter(id));
        SaudeDTO s = pacientes.saude(id);
        m.addAttribute("problemas", String.join("\n", s.problemas()));
        m.addAttribute("sintomas", String.join("\n", s.sintomas()));
        return "paciente-saude-form";
    }

    @PostMapping("/{id}/saude")
    public String saudeSalvar(@PathVariable Long id,
                              @RequestParam(name = "problemas", defaultValue = "") String problemas,
                              @RequestParam(name = "sintomas", defaultValue = "") String sintomas,
                              RedirectAttributes ra) {
        obter(id);
        pacientes.salvarSaude(id, new SaudeDTO(linhas(problemas), linhas(sintomas)));
        ra.addFlashAttribute("sucesso", "Informações de saúde atualizadas.");
        return "redirect:/pacientes/" + id + "/saude";
    }

    private List<String> linhas(String texto) {
        return Arrays.stream(texto.split("\\R")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    // ---------- observações ----------
    @GetMapping("/{id}/observacoes")
    public String observacoes(@PathVariable Long id, Model m) {
        m.addAttribute("paciente", obter(id));
        m.addAttribute("observacoes", pacientes.observacoes(id));
        return "paciente-observacoes";
    }

    @GetMapping("/{id}/observacoes/nova")
    public String observacaoNova(@PathVariable Long id, Model m) {
        m.addAttribute("paciente", obter(id));
        return "paciente-observacao-form";
    }

    @PostMapping("/{id}/observacoes")
    public String observacaoSalvar(@PathVariable Long id, @RequestParam(name = "texto", defaultValue = "") String texto,
                                   HttpSession sessao, Model m, RedirectAttributes ra) {
        PacienteDTO p = obter(id);
        String t = texto.trim();
        if (t.isEmpty() || t.length() > 1000) {
            m.addAttribute("paciente", p);
            m.addAttribute("texto", texto);
            m.addAttribute("erros", List.of(t.isEmpty()
                    ? "Escreva a observação antes de salvar."
                    : "A observação pode ter no máximo 1000 caracteres."));
            return "paciente-observacao-form";
        }
        SessaoUsuario usuario = (SessaoUsuario) sessao.getAttribute(SessaoUsuario.CHAVE);
        pacientes.adicionarObservacao(id, t, usuario == null ? "" : usuario.getNome());
        ra.addFlashAttribute("sucesso", "Observação registrada.");
        return "redirect:/pacientes/" + id + "/observacoes";
    }
}
