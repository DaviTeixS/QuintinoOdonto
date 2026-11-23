package br.com.quintino.odonto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Funcionário que realiza os atendimentos; tem horários de atendimento e agenda própria. */
@Entity
@Table(name = "dentistas")
public class Dentista extends Funcionario {

    @Column(length = 40)
    private String cro;

    @Column(length = 80)
    private String especialidade;

    public Dentista() {
        setPerfil(Perfil.DENTISTA);
    }

    public String getCro() {
        return cro;
    }

    public void setCro(String cro) {
        this.cro = cro;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
}
