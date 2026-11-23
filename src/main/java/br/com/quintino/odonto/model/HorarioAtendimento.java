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
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** Período semanal em que um dentista está disponível para atender. */
@Entity
@Table(name = "horarios_atendimento")
public class HorarioAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dentista_id", nullable = false)
    private Dentista dentista;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 12)
    private DayOfWeek diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    public HorarioAtendimento() {
    }

    public HorarioAtendimento(Dentista dentista, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        this.dentista = dentista;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Long getId() {
        return id;
    }

    public Dentista getDentista() {
        return dentista;
    }

    public DayOfWeek getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }
}
