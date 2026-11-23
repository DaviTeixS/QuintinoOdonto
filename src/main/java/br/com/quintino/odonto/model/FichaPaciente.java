package br.com.quintino.odonto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Informações de saúde do paciente (a ficha não existe sem o paciente).
 * Os campos de texto guardam um item por linha. "sintomas" guarda os sintomas informados
 * pelo paciente (tela de saúde, RF23).
 */
@Entity
@Table(name = "fichas_pacientes")
public class FichaPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 4000)
    private String problemasDeSaude;

    @Column(length = 4000)
    private String alergias;

    @Column(length = 4000)
    private String medicamentosEmUso;

    @Column(length = 4000)
    private String sintomas;

    @OneToOne(optional = false)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    public FichaPaciente() {
    }

    public FichaPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Long getId() {
        return id;
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

    public String getSintomas() {
        return sintomas;
    }

    public void setSintomas(String sintomas) {
        this.sintomas = sintomas;
    }

    public Paciente getPaciente() {
        return paciente;
    }
}
