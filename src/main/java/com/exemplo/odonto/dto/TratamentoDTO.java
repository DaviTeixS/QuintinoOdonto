package br.com.quintino.odonto.dto;

import java.util.List;

/** status: EM_ANDAMENTO | CONCLUIDO. etapas = consultas do tratamento. */
public record TratamentoDTO(Long id, String titulo, String status, List<ConsultaDTO> etapas) {
}
