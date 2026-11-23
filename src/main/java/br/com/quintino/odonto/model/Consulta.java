package br.com.quintino.odonto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Agendamento de um paciente com um dentista para um procedimento, em data e hora definidas. */
@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dentista_id", nullable = false)
    private Dentista dentista;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false)
    private Procedimento procedimento;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusConsulta status = StatusConsulta.AGENDADA;

    @Column(name = "sintomas_informados", length = 1000)
    private String sintomasInformados;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    /** Dente tratado (opcional, ex.: "26"). */
    @Column(length = 10)
    private String dente;

    /** Observações do atendimento (compõem a consulta; só podem ser incluídas). */
    @OneToMany(mappedBy = "consulta")
    private List<Observacao> observacoes = new ArrayList<>();

    public Consulta() {
    }

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Dentista getDentista() {
        return dentista;
    }

    public void setDentista(Dentista dentista) {
        this.dentista = dentista;
    }

    public Procedimento getProcedimento() {
        return procedimento;
    }

    public void setProcedimento(Procedimento procedimento) {
        this.procedimento = procedimento;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    public String getSintomasInformados() {
        return sintomasInformados;
    }

    public void setSintomasInformados(String sintomasInformados) {
        this.sintomasInformados = sintomasInformados;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public String getDente() {
        return dente;
    }

    public void setDente(String dente) {
        this.dente = dente;
    }

    public List<Observacao> getObservacoes() {
        return observacoes;
    }
}
