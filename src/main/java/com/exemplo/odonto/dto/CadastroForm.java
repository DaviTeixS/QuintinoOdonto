package br.com.quintino.odonto.dto;

import java.time.LocalDate;

/**
 * Dados do cadastro de colaborador (tela "Fazer cadastro" e "+ Novo Colaborador").
 * A senha chega em texto puro: o back-end precisa gerar o hash (BCrypt) antes de salvar.
 * funcao pode vir nula no cadastro público.
 */
public record CadastroForm(String nome, String documento, String telefone, LocalDate nascimento,
                           String email, String senha, String funcao) {
}
