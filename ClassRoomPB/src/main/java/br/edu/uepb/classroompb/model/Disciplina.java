package br.edu.uepb.classroompb.model;

import java.util.ArrayList;
import java.util.List;

public class Disciplina {
    private String codigo;
    private String nome;
    private int cargaHoraria;
    private int creditos;
    private List<String> preRequisitosCodigos;

    public Disciplina() {
        this.preRequisitosCodigos = new ArrayList<>();
    }

    public Disciplina(String codigo, String nome, int cargaHoraria, int creditos, List<String> preRequisitosCodigos) {
        this.codigo = codigo;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
        this.creditos = creditos;
        this.preRequisitosCodigos = preRequisitosCodigos != null ? preRequisitosCodigos : new ArrayList<>();
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(int cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public int getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }

    public List<String> getPreRequisitosCodigos() {
        return preRequisitosCodigos;
    }

    public void setPreRequisitosCodigos(List<String> preRequisitosCodigos) {
        this.preRequisitosCodigos = preRequisitosCodigos;
    }

    @Override
    public String toString() {
        // Une os pré-requisitos por vírgula ou deixa vazio se não houver nenhum
        String preRequisitosStr = String.join(",", preRequisitosCodigos);
        if (preRequisitosStr.isEmpty()) {
            preRequisitosStr = "NENHUM";
        }
        return codigo + ";" + nome + ";" + cargaHoraria + ";" + creditos + ";" + preRequisitosStr;
    }
}