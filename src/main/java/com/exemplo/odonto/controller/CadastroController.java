package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.service.ColaboradorService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Tela "Fazer cadastro" (link do login). Página pública. */
@Controller
public class CadastroController {

    private final ColaboradorService colaboradores;

    public CadastroController(ColaboradorService colaboradores) {
        this.colaboradores = colaboradores;
    }

    private void pagina(Model m, Map<String, String> v) {
        m.addAttribute("v", v);
        m.addAttribute("acao", "/cadastro");
        m.addAttribute("tituloPagina", "Cadastro");
        m.addAttribute("voltar", "/login");
        m.addAttribute("mostrarFuncao", false);
        m.addAttribute("hoje", LocalDate.now().toString());
    }

    @GetMapping("/cadastro")
    public String formulario(Model m) {
        pagina(m, Map.of());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam Map<String, String> v, Model m, RedirectAttributes ra) {
        List<String> erros = new ArrayList<>();
        CadastroValidador.validar(v, false, erros);
        if (erros.isEmpty()) {
            try {
                colaboradores.cadastrar(CadastroValidador.montar(v));
                ra.addFlashAttribute("sucesso", "Cadastro realizado! Entre com seu e-mail e senha.");
                return "redirect:/login";
            } catch (IllegalArgumentException e) {
                erros.add(e.getMessage());
            }
        }
        m.addAttribute("erros", erros);
        pagina(m, CadastroValidador.semSenhas(v));
        return "cadastro";
    }
}
