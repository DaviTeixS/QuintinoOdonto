package br.com.quintino.odonto.dto;

/** Item genérico das listas (pacientes, colaboradores, procedimentos, dentistas). */
public record ItemListaDTO(Long id, String titulo, String subtitulo, String iniciais) {
}
