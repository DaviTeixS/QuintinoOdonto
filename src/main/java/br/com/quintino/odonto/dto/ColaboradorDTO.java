package br.com.quintino.odonto.dto;

/** funcao: "DENTISTA" ou "RECEPCIONISTA". */
public record ColaboradorDTO(Long id, String nome, String funcao, String email,
                             String celular, String cpf, String cro) {
}
