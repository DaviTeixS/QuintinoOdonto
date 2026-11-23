package br.com.quintino.odonto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

/**
 * Anotação do dentista sobre o paciente. Só pode ser incluída, nunca alterada (RNF10):
 * a entidade é @Immutable e não tem setters. Guarda autor e data/hora (RNF11).
 * A consulta é opcional: fica ligada à consulta mais recente do paciente, quando existe.
 */
@Entity
@Immutable
@Table(name = "observacoes")
public class Observacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000, updatable = false)
    private String texto;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id", updatable = false)
    private Consulta consulta;

    @Column(length = 100, updatable = false)
    private String autor;

    protected Observacao() {
    }

    public Observacao(Paciente paciente, Consulta consulta, String texto, String autor) {
        this.paciente = paciente;
        this.consulta = consulta;
        this.texto = texto;
        this.autor = autor;
        this.dataHora = LocalDateTime.now();
    }

    /** Usado pela carga de dados de demonstração, para registrar observações com data passada. */
    public Observacao(Paciente paciente, Consulta consulta, String texto, String autor, LocalDateTime dataHora) {
        this(paciente, consulta, texto, autor);
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public String getAutor() {
        return autor;
    }
}
