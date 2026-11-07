package com.exemplo.mvc.model;

import jakarta.persistence.*;

@Entity
@Table(name = "FichaPaciente")
public class FichaPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String problemasDeSaude;
    private String alergias;
    private String medicamentosEmUso;


    @OneToOne
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    public FichaPaciente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProblemasDeSaude() {
        return problemasDeSaude;
    }

    public void setProblemasDeSaude(String problemasDeSaude) {
        this.problemasDeSaude = problemasDeSaude;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getMedicamentosEmUso() {
        return medicamentosEmUso;
    }

    public void setMedicamentosEmUso(String medicamentosEmUso) {
        this.medicamentosEmUso = medicamentosEmUso;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
}
