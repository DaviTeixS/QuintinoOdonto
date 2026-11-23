package br.com.quintino.odonto.dto;

import br.com.quintino.odonto.util.Fmt;
import java.time.LocalDate;
import java.time.LocalTime;

/** status: AGENDADA | REALIZADA | CANCELADA. dente: opcional (ex.: "26"). */
public record ConsultaDTO(Long id, Long pacienteId, String pacienteNome,
                          Long dentistaId, String dentistaNome, String procedimento,
                          LocalDate data, LocalTime hora, String status, String dente) {

    public String dataBr() {
        return Fmt.I.data(data);
    }

    public String horaFmt() {
        return Fmt.I.hora(hora);
    }
}
