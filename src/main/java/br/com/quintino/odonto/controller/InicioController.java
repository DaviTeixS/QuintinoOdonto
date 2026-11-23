package br.com.quintino.odonto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    @GetMapping("/")
    public String raiz() {
        return "redirect:/inicio";
    }

    /** Tela inicial: calendário (calendario.js busca os dados em /api/agenda/...). */
    @GetMapping("/inicio")
    public String inicio() {
        return "inicio";
    }
}
