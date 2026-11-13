package com.exemplo.odonto.model;

public class Procedimento {

    private Long id;
    private String nome;
    private String descricao;
    private int duracaoMinutos;
    private boolean ativo;

    //ativação do procedimento
    public void ativar(){
        this.ativo=true;
    }

    //desativando do procedimento
    public void desativar(){
        this.ativo=false;
    }

}
