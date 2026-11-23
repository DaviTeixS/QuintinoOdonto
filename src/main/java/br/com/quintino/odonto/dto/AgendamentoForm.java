package br.com.quintino.odonto.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** Dados do formulário de agendamento (novo e alteração). Sintomas e dente são opcionais. */
public record AgendamentoForm(Long pacienteId, Long dentistaId, Long procedimentoId,
                              LocalDate data, LocalTime hora, String sintomas, String dente) {
}
