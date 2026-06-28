package br.edu.uepb.classroompb.model;

import java.util.List;
import java.util.ArrayList;

public class Turma {
    private String codigoDisciplina;
    private String matriculaProfessor;
    private String periodo;
    private int vagas;
    private int vagasOcupadas; 
    private String horario;
    private String sala;
    private List<String> listaEsperaMatriculas; // REQUISITO CENTRAL DA TASK 2273
    public boolean setTurmaEncontrada;

    // Construtor principal adaptado para iniciar com 0 vagas ocupadas e lista de espera vazia
    public Turma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, String horario, String sala) {
        this.codigoDisciplina = codigoDisciplina;
        this.matriculaProfessor = matriculaProfessor;
        this.periodo = periodo;
        this.vagas = vagas;
        this.vagasOcupadas = 0; 
        this.horario = horario;
        this.sala = sala;
        this.listaEsperaMatriculas = new ArrayList<>(); // Inicializa a fila encadeada limpa
    }

    // Construtor secundário para restauração de persistência do arquivo físico
    public Turma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, int vagasOcupadas, String horario, String sala) {
        this.codigoDisciplina = codigoDisciplina;
        this.matriculaProfessor = matriculaProfessor;
        this.periodo = periodo;
        this.vagas = vagas;
        this.vagasOcupadas = vagasOcupadas;
        this.horario = horario;
        this.sala = sala;
        this.listaEsperaMatriculas = new ArrayList<>();
    }

    // ====================================================================
    // REGRAS DE ESTILO COMPORTAMENTAIS - MANIPULAÇÃO FIFO (TASK 2273)
    // ====================================================================
    /**
     * Insere de forma estrita o identificador do estudante na cauda (tail) da fila.
     */
    public void enfileirarEstudante(String matriculaAluno) {
        if (this.listaEsperaMatriculas == null) {
            this.listaEsperaMatriculas = new ArrayList<>();
        }
        if (!this.listaEsperaMatriculas.contains(matriculaAluno)) {
            this.listaEsperaMatriculas.add(matriculaAluno);
        }
    }

    // Getters
    public String getCodigoDisciplina() { return codigoDisciplina; }
    public String getMatriculaProfessor() { return matriculaProfessor; }
    public String getPeriodo() { return periodo; }
    public int getVagas() { return vagas; }
    public int getVagasOcupadas() { return vagasOcupadas; } 
    public String getHorario() { return horario; }
    public String getSala() { return sala; }
    public List<String> getListaEsperaMatriculas() { 
        if (listaEsperaMatriculas == null) {
            listaEsperaMatriculas = new ArrayList<>();
        }
        return listaEsperaMatriculas; 
    }

    // Setters 
    public void setVagas(int vagas) { this.vagas = vagas; }
    public void setVagasOcupadas(int vagasOcupadas) { this.vagasOcupadas = vagasOcupadas; } 
    public void setHorario(String horario) { this.horario = horario; }
    public void setSala(String sala) { this.sala = sala; }
    public void setListaEsperaMatriculas(List<String> listaEsperaMatriculas) { this.listaEsperaMatriculas = listaEsperaMatriculas; }

    @Override
    public String toString() {
        String filaSerializada = listaEsperaMatriculas != null ? String.join(",", listaEsperaMatriculas) : "";
        if (filaSerializada.isEmpty()) {
            filaSerializada = "VAZIA";
        }
        return codigoDisciplina + ";" + matriculaProfessor + ";" + periodo + ";" + vagas + ";" + vagasOcupadas + ";" + horario + ";" + sala + ";" + filaSerializada;
    }
}