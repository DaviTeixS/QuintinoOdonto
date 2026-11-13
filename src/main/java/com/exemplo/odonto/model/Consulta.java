package com.exemplo.odonto.model;

import java.time.LocalDateTime;

import static com.exemplo.odonto.model.StatusConsulta.AGENDADA;

public class Consulta {

    private Long id;
    private LocalDateTime dataHora;
    private  StatusConsulta status;
    private String sintomasInformados;
    private String motivoCancelamento;


    //construters
    public Consulta(){}


    public Consulta(Long id, LocalDateTime dataHora, StatusConsulta status, String sintomasInformados, String motivoCancelamento) {
        this.id = id;
        this.dataHora = dataHora;
        this.status = status;
        this.sintomasInformados = sintomasInformados;
        this.motivoCancelamento = motivoCancelamento;
    }

    //getters e setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    //métodos




}
