package br.edu.uepb.classroompb.model;

import java.util.ArrayList;
import java.util.List;

public class Turma {
    private String codigoDisciplina;
    private String matriculaProfessor;
    private String periodo;
    private int vagas;
    private int vagasOcupadas; 
    private String horario;
    private String sala;
    public boolean setTurmaEncontrada;
    
    // [TASK 2273] Estrutura de modelagem ordenada para controle de fila da turma
    private List<String> listaEsperaMatriculas = new ArrayList<>();

    public Turma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, String horario, String sala) {
        this.codigoDisciplina = codigoDisciplina;
        this.matriculaProfessor = matriculaProfessor;
        this.periodo = periodo;
        this.vagas = vagas;
        this.vagasOcupadas = 0; 
        this.horario = horario;
        this.sala = sala;
    }

    public Turma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, int vagasOcupadas, String horario, String sala) {
        this.codigoDisciplina = codigoDisciplina;
        this.matriculaProfessor = matriculaProfessor;
        this.periodo = periodo;
        this.vagas = vagas;
        this.vagasOcupadas = vagasOcupadas;
        this.horario = horario;
        this.sala = sala;
    }

    // Getters e Setters da Fila (Task 2273)
    public List<String> getListaEsperaMatriculas() { return listaEsperaMatriculas; }
    public void setListaEsperaMatriculas(List<String> listaEsperaMatriculas) { this.listaEsperaMatriculas = listaEsperaMatriculas; }

    // Getters
    public String getCodigoDisciplina() { return codigoDisciplina; }
    public String getMatriculaProfessor() { return matriculaProfessor; }
    public String getPeriodo() { return periodo; }
    public int getVagas() { return vagas; }
    public int getVagasOcupadas() { return vagasOcupadas; } 
    public String getHorario() { return horario; }
    public String getSala() { return sala; }

    // Setters 
    public void setVagas(int vagas) { this.vagas = vagas; }
    public void setVagasOcupadas(int vagasOcupadas) { this.vagasOcupadas = vagasOcupadas; } 
    public void setHorario(String horario) { this.horario = horario; }
    public void setSala(String sala) { this.sala = sala; }

    @Override
    public String toString() {
        return codigoDisciplina + ";" + matriculaProfessor + ";" + periodo + ";" + vagas + ";" + vagasOcupadas + ";" + horario + ";" + sala;
    }
}