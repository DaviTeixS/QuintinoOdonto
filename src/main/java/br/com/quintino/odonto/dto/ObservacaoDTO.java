package br.com.quintino.odonto.dto;

import br.com.quintino.odonto.util.Fmt;
import java.time.LocalDate;

public record ObservacaoDTO(Long id, String texto, LocalDate data, String autor) {

    public String dataBr() {
        return Fmt.I.data(data);
    }
}
