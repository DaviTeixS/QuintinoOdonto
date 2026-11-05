package br.com.quintino.odonto.config;

import br.com.quintino.odonto.model.Perfil;

public class SessaoUsuario {

    public static final String CHAVE = "usuarioAutenticado";

    private final Long id;
    private final String nome;
    private final Perfil perfil;

    public SessaoUsuario(Long id, String nome, Perfil perfil) {
        this.id = id;
        this.nome = nome;
        this.perfil = perfil;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    /** Usado nos templates (menu mostra Colaborador/Procedimentos só para o consultório). */
    public boolean isConsultorio() {
        return perfil == Perfil.CONSULTORIO;
    }
}