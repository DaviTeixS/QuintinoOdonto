package br.com.quintino.odonto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/** Serviço odontológico oferecido pela clínica; a duração é usada para ocupar a agenda. */
@Entity
@Table(name = "procedimentos")
public class Procedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos;

    @Column(nullable = false)
    private boolean ativo = true;

    /** Dentistas que realizam este procedimento. */
    @ManyToMany
    @JoinTable(name = "procedimentos_dentistas",
            joinColumns = @JoinColumn(name = "procedimento_id"),
            inverseJoinColumns = @JoinColumn(name = "dentista_id"))
    private Set<Dentista> dentistas = new HashSet<>();

    public Procedimento() {
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(int duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Set<Dentista> getDentistas() {
        return dentistas;
    }

    public void setDentistas(Set<Dentista> dentistas) {
        this.dentistas = dentistas;
    }
}
