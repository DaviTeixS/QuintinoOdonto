package br.com.quintino.odonto.dto;

import br.com.quintino.odonto.util.Fmt;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** Período semanal de atendimento de um dentista. */
public record HorarioDTO(Long id, DayOfWeek diaSemana, LocalTime inicio, LocalTime fim) {

    public String diaNome() {
        return switch (diaSemana) {
            case MONDAY -> "Segunda-feira";
            case TUESDAY -> "Terça-feira";
            case WEDNESDAY -> "Quarta-feira";
            case THURSDAY -> "Quinta-feira";
            case FRIDAY -> "Sexta-feira";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    public String inicioFmt() {
        return Fmt.I.hora(inicio);
    }

    public String fimFmt() {
        return Fmt.I.hora(fim);
    }
}
