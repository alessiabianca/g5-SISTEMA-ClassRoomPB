// src/main/java/br/edu/uepb/classroompb/model/Turma.java
package br.edu.uepb.classroompb.model;

public class Turma {
    private String codigoDisciplina;
    private String matriculaProfessor;
    private String periodo;
    private int vagas;
    private String horario;
    private String sala;
    public boolean setTurmaEncontrada;

    public Turma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, String horario, String sala) {
        this.codigoDisciplina = codigoDisciplina;
        this.matriculaProfessor = matriculaProfessor;
        this.periodo = periodo;
        this.vagas = vagas;
        this.horario = horario;
        this.sala = sala;
    }

    // Getters
    public String getCodigoDisciplina() { return codigoDisciplina; }
    public String getMatriculaProfessor() { return matriculaProfessor; }
    public String getPeriodo() { return periodo; }
    public int getVagas() { return vagas; }
    public String getHorario() { return horario; }
    public String getSala() { return sala; }

    // Setters 
    public void setVagas(int vagas) { this.vagas = vagas; }
    public void setHorario(String horario) { this.horario = horario; }
    public void setSala(String sala) { this.sala = sala; }

    @Override
    public String toString() {
        return codigoDisciplina + ";" + matriculaProfessor + ";" + periodo + ";" + vagas + ";" + horario + ";" + sala;
    }
}