package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.config.SessaoUsuario;
import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.service.ColaboradorService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Tela "Agendas" (RF11, RF19, RF20): visões Dia, Semana e Mês.
 * - Dentista: vê só a própria agenda.
 * - Consultório: vê a agenda geral ou escolhe um dentista na lista ("Todos os dentistas").
 * Os dados são carregados pelo JavaScript (calendario.js e agenda.js) a partir de /api/agenda/*.
 */
@Controller
public class AgendaController {

    private static final List<String> VISOES = List.of("dia", "semana", "mes");

    private final ColaboradorService colaboradores;

    public AgendaController(ColaboradorService colaboradores) {
        this.colaboradores = colaboradores;
    }

    @GetMapping("/agenda")
    public String agenda(@RequestParam(name = "visao", defaultValue = "mes") String visao,
                         @RequestParam(name = "data", required = false) String data,
                         @RequestParam(name = "dentistaId", required = false) Long dentistaId,
                         HttpSession sessao, Model m) {
        SessaoUsuario usuario = (SessaoUsuario) sessao.getAttribute(SessaoUsuario.CHAVE);
        LocalDate dia = data == null ? null : Validacao.data(data);
        if (dia == null) {
            dia = LocalDate.now();
        }

        Long filtro = null;
        String filtroNome = null;
        if (usuario != null && usuario.isConsultorio()) {
            List<ItemListaDTO> dentistas = colaboradores.dentistas();
            m.addAttribute("listaDentistas", dentistas);
            for (ItemListaDTO d : dentistas) {
                if (d.id().equals(dentistaId)) {
                    filtro = d.id();
                    filtroNome = d.titulo();
                }
            }
        }

        m.addAttribute("visao", VISOES.contains(visao) ? visao : "mes");
        m.addAttribute("dataIso", dia.toString());
        m.addAttribute("dentistaId", filtro);
        m.addAttribute("dentistaNome", filtroNome);
        m.addAttribute("visoes", VISOES);
        return "agenda";
    }
}
