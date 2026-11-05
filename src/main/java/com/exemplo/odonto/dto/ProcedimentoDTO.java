package br.com.quintino.odonto.dto;

import java.util.List;

public record ProcedimentoDTO(Long id, String nome, int duracaoMinutos, String descricao,
                              List<Long> dentistaIds, List<String> dentistaNomes) {
}
