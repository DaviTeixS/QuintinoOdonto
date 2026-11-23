package br.com.quintino.odonto.dto;

import br.com.quintino.odonto.util.Fmt;
import java.time.LocalDate;
import java.time.LocalTime;

/** Consulta completa (tela de detalhe e formulário de alteração). */
public record ConsultaDetalheDTO(Long id, Long pacienteId, String pacienteNome,
                                 Long dentistaId, String dentistaNome,
                                 Long procedimentoId, String procedimento, int duracaoMinutos,
                                 LocalDate data, LocalTime hora, String status,
                                 String sintomas, String motivoCancelamento, String dente) {

    public String dataBr() {
        return Fmt.I.data(data);
    }

    public String horaFmt() {
        return Fmt.I.hora(hora);
    }

    /** "Quinta, 03/09 – 10:00" (tela de reagendar). */
    public String quandoCurto() {
        return Fmt.I.diaSemana(data) + ", " + Fmt.I.diaMes(data) + " – " + Fmt.I.hora(hora);
    }

    public boolean agendada() {
        return "AGENDADA".equals(status);
    }
}
