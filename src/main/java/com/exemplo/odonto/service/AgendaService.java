package br.com.quintino.odonto.service;

import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.DiaAgendaDTO;
import java.time.LocalDate;
import java.util.List;

/** CONTRATO DO BACK-END: dados do calendário da tela inicial. */
public interface AgendaService {

    /** Dias do mês (1..31) que têm consultas e quantas são. Ignorar canceladas. */
    List<DiaAgendaDTO> diasComConsulta(int ano, int mes);

    /** Consultas do dia ordenadas por hora. */
    List<ConsultaDTO> consultasDoDia(LocalDate data);
}
