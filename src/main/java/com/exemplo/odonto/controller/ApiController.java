package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.DiaAgendaDTO;
import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.service.AgendaService;
import br.com.quintino.odonto.service.ColaboradorService;
import br.com.quintino.odonto.service.PacienteService;
import br.com.quintino.odonto.service.ProcedimentoService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints JSON consumidos pelo JavaScript das telas (busca das listas, calendário, listas suspensas).
 * Nenhum dado é montado aqui: tudo vem dos serviços (contrato do back-end).
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
    public List<DiaAgendaDTO> agendaMes(@RequestParam("ano") int ano, @RequestParam("mes") int mes) {
        return agenda.diasComConsulta(ano, mes);
    }

    @GetMapping("/agenda/dia")
    public List<ConsultaDTO> agendaDia(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return agenda.consultasDoDia(data);
    }
}
