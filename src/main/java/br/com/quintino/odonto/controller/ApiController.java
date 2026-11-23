package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.config.SessaoUsuario;
import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.DiaAgendaDTO;
import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.service.AgendaService;
import br.com.quintino.odonto.service.ColaboradorService;
import br.com.quintino.odonto.service.PacienteService;
import br.com.quintino.odonto.service.ProcedimentoService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints JSON consumidos pelo JavaScript das telas (busca das listas, calendário, listas suspensas).
 * Nenhum dado é montado aqui: tudo vem dos serviços.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final PacienteService pacientes;
    private final ColaboradorService colaboradores;
    private final ProcedimentoService procedimentos;
    private final AgendaService agenda;

    public ApiController(PacienteService pacientes, ColaboradorService colaboradores,
                         ProcedimentoService procedimentos, AgendaService agenda) {
        this.pacientes = pacientes;
        this.colaboradores = colaboradores;
        this.procedimentos = procedimentos;
        this.agenda = agenda;
    }

    /**
     * Dentista só enxerga a própria agenda (RF19/RF20); o consultório vê a agenda geral (RF11)
     * ou a de um dentista, quando escolhe um na tela "Agendas".
     */
    private Long escopoDentista(HttpSession sessao, Long dentistaSolicitado) {
        SessaoUsuario usuario = (SessaoUsuario) sessao.getAttribute(SessaoUsuario.CHAVE);
        if (usuario != null && usuario.isDentista()) {
            return usuario.getId();
        }
        return dentistaSolicitado;
    }

    @GetMapping("/pacientes")
    public List<ItemListaDTO> pacientes(@RequestParam(name = "busca", defaultValue = "") String busca) {
        return pacientes.buscar(busca);
    }

    @GetMapping("/colaboradores")
    public List<ItemListaDTO> colaboradores(@RequestParam(name = "busca", defaultValue = "") String busca) {
        return colaboradores.buscar(busca);
    }

    @GetMapping("/dentistas")
    public List<ItemListaDTO> dentistas() {
        return colaboradores.dentistas();
    }

    @GetMapping("/procedimentos")
    public List<ItemListaDTO> procedimentos(@RequestParam(name = "busca", defaultValue = "") String busca) {
        return procedimentos.buscar(busca);
    }

    @GetMapping("/agenda/mes")
    public List<DiaAgendaDTO> agendaMes(@RequestParam("ano") int ano, @RequestParam("mes") int mes,
                                        @RequestParam(name = "dentistaId", required = false) Long dentistaId,
                                        HttpSession sessao) {
        return agenda.diasComConsulta(ano, mes, escopoDentista(sessao, dentistaId));
    }

    @GetMapping("/agenda/dia")
    public List<ConsultaDTO> agendaDia(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(name = "dentistaId", required = false) Long dentistaId,
            HttpSession sessao) {
        return agenda.consultasDoDia(data, escopoDentista(sessao, dentistaId));
    }

    /** Sete dias a partir de "inicio" (visão semanal da agenda). */
    @GetMapping("/agenda/semana")
    public List<ConsultaDTO> agendaSemana(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "dentistaId", required = false) Long dentistaId,
            HttpSession sessao) {
        return agenda.consultasDoPeriodo(inicio, inicio.plusDays(7), escopoDentista(sessao, dentistaId));
    }

    /** Horários de início livres de um dentista (alimenta a lista de horários do agendamento). */
    @GetMapping("/horarios-livres")
    public List<String> horariosLivres(
            @RequestParam("dentistaId") Long dentistaId,
            @RequestParam("procedimentoId") Long procedimentoId,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(name = "ignorar", required = false) Long ignorarConsultaId) {
        return agenda.horariosLivres(dentistaId, procedimentoId, data, ignorarConsultaId);
    }
}
