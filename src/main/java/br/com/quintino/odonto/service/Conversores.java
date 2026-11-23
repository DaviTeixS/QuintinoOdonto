package br.com.quintino.odonto.service;

import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.ConsultaDetalheDTO;
import br.com.quintino.odonto.model.Consulta;

/** Conversões Entidade -> DTO compartilhadas pelos serviços (chamar dentro de uma transação). */
final class Conversores {

    private Conversores() {
    }

    static String nvl(String s) {
        return s == null ? "" : s;
    }

    static ConsultaDTO paraDto(Consulta c) {
        return new ConsultaDTO(c.getId(),
                c.getPaciente().getId(), c.getPaciente().getNome(),
                c.getDentista().getId(), c.getDentista().getNome(),
                c.getProcedimento().getNome(),
                c.getDataHora().toLocalDate(), c.getDataHora().toLocalTime(),
                c.getStatus().name(), c.getDente());
    }

    static ConsultaDetalheDTO detalhe(Consulta c) {
        return new ConsultaDetalheDTO(c.getId(),
                c.getPaciente().getId(), c.getPaciente().getNome(),
                c.getDentista().getId(), c.getDentista().getNome(),
                c.getProcedimento().getId(), c.getProcedimento().getNome(),
                c.getProcedimento().getDuracaoMinutos(),
                c.getDataHora().toLocalDate(), c.getDataHora().toLocalTime(),
                c.getStatus().name(),
                nvl(c.getSintomasInformados()), nvl(c.getMotivoCancelamento()), nvl(c.getDente()));
    }
}
