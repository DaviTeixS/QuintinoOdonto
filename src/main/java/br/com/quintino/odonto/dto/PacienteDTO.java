package br.com.quintino.odonto.dto;

import br.com.quintino.odonto.util.Fmt;
import java.time.LocalDate;

/** Dados cadastrais do paciente (telas Cadastro / Editar / Novo paciente). */
public record PacienteDTO(Long id, String nome, String sexo, LocalDate nascimento,
                          String celular, String rg, String cpf) {

    public String nascimentoBr() {
        return Fmt.I.data(nascimento);
    }
}
