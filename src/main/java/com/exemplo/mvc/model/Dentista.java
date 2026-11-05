package com.exemplo.mvc.model;



//add metodos
public class Dentista extends Funcionario {

    private String cro;
    private String especialidade;


    public Dentista() { }


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
