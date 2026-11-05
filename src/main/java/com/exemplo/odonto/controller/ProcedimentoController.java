package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.dto.ProcedimentoDTO;
import br.com.quintino.odonto.service.ProcedimentoService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

/** Telas de procedimentos (somente perfil CONSULTORIO). */
@Controller
@RequestMapping("/procedimentos")
public class ProcedimentoController {

    private static final List<Integer> DURACOES = List.of(15, 30, 45, 60, 75, 90, 120, 150, 180);

    private final ProcedimentoService procedimentos;

    public ProcedimentoController(ProcedimentoService procedimentos) {
        this.procedimentos = procedimentos;
    }

    private ProcedimentoDTO obter(Long id) {
        return procedimentos.buscarPorId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void paginaForm(Model m, Map<String, String> v, String acao, String titulo) {
        m.addAttribute("v", v);
        m.addAttribute("acao", acao);
        m.addAttribute("tituloPagina", titulo);
        m.addAttribute("duracoes", DURACOES);
    }

    @GetMapping
    public String lista() {
        return "procedimentos";
    }

    @GetMapping("/novo")
    public String novo(Model m) {
        Map<String, String> v = new HashMap<>();
        v.put("duracao", "30");
        paginaForm(m, v, "/procedimentos", "Novo procedimento");
        return "procedimento-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m) {
        ProcedimentoDTO p = obter(id);
        Map<String, String> v = new HashMap<>();
        v.put("nome", p.nome());
        v.put("duracao", String.valueOf(p.duracaoMinutos()));
        v.put("descricao", p.descricao());
        v.put("selecionados", p.dentistaIds() == null ? ""
                : p.dentistaIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        paginaForm(m, v, "/procedimentos/" + id, "Editar procedimento");
        return "procedimento-form";
    }

    @PostMapping
    public String criar(@RequestParam Map<String, String> v,
                        @RequestParam(name = "dentistaIds", required = false) List<Long> dentistaIds,
                        Model m, RedirectAttributes ra) {
        return salvar(null, v, dentistaIds, m, ra);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @RequestParam Map<String, String> v,
                            @RequestParam(name = "dentistaIds", required = false) List<Long> dentistaIds,
                            Model m, RedirectAttributes ra) {
        obter(id);
        return salvar(id, v, dentistaIds, m, ra);
    }

    private String salvar(Long id, Map<String, String> v, List<Long> dentistaIds, Model m, RedirectAttributes ra) {
        List<String> erros = new ArrayList<>();
        String nome = Validacao.texto(v, "nome");
        String descricao = Validacao.texto(v, "descricao");
        int duracao = 0;
        try {
            duracao = Integer.parseInt(Validacao.texto(v, "duracao"));
        } catch (NumberFormatException e) {
            erros.add("Selecione a duração estimada.");
        }
        if (nome.length() < 2) {
            erros.add("Informe o nome do procedimento.");
        }
        if (duracao != 0 && (duracao < 5 || duracao > 480)) {
            erros.add("A duração deve ficar entre 5 minutos e 8 horas.");
        }
        if (dentistaIds == null || dentistaIds.isEmpty()) {
            erros.add("Selecione pelo menos uma dentista responsável.");
        }
        if (!erros.isEmpty()) {
            Map<String, String> repreencher = new HashMap<>(v);
            repreencher.put("selecionados", dentistaIds == null ? ""
                    : dentistaIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            m.addAttribute("erros", erros);
            paginaForm(m, repreencher, id == null ? "/procedimentos" : "/procedimentos/" + id,
                    id == null ? "Novo procedimento" : "Editar procedimento");
            return "procedimento-form";
        }
        procedimentos.salvar(new ProcedimentoDTO(id, nome, duracao, descricao, dentistaIds, List.of()));
        ra.addFlashAttribute("sucesso", id == null ? "Procedimento cadastrado." : "Procedimento atualizado.");
        return "redirect:/procedimentos";
    }
}
