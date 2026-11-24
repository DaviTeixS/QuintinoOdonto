package br.com.quintino.odonto.config;

import br.com.quintino.odonto.model.Perfil;

/** Usuário logado, guardado na sessão HTTP. O id é o do funcionário (e do dentista, quando for um). */
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

    /**
     * "Consultório" = administrativo e recepção: cadastram pacientes, colaboradores, procedimentos
     * e horários e fazem os agendamentos. Usado nos templates (${usuario.consultorio}).
     */

    public boolean isAdministrativo(){return perfil==Perfil.ADMINISTRATIVO;
    }

    public boolean isConsultorio() {
        return perfil == Perfil.ADMINISTRATIVO || perfil == Perfil.RECEPCAO;
    }

    /** Dentista: vê só a própria agenda e registra observações nas fichas. */
    public boolean isDentista() {
        return perfil == Perfil.DENTISTA;
    }
}
