// src/main/java/br/edu/uepb/classroompb/model/Turma.java
package br.edu.uepb.classroompb.model;

public class Turma {
    private String codigoDisciplina;
    private String matriculaProfessor;
    private String periodo;
    private int vagas;
    private int vagasOcupadas; // REQUISITO TASK 2108: Novo atributo de controle
    private String horario;
    private String sala;
    public boolean setTurmaEncontrada;

    // Construtor principal adaptado para iniciar com 0 vagas ocupadas
    public Turma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, String horario, String sala) {
        this.codigoDisciplina = codigoDisciplina;
        this.matriculaProfessor = matriculaProfessor;
        this.periodo = periodo;
        this.vagas = vagas;
        this.vagasOcupadas = 0; // Toda turma nova começa vazia
        this.horario = horario;
        this.sala = sala;
    }

    // Construtor secundário útil para quando o repositório ler os dados salvos do arquivo físico
    public Turma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, int vagasOcupadas, String horario, String sala) {
        this.codigoDisciplina = codigoDisciplina;
        this.matriculaProfessor = matriculaProfessor;
        this.periodo = periodo;
        this.vagas = vagas;
        this.vagasOcupadas = vagasOcupadas;
        this.horario = horario;
        this.sala = sala;
    }

    // Getters
    public String getCodigoDisciplina() { return codigoDisciplina; }
    public String getMatriculaProfessor() { return matriculaProfessor; }
    public String getPeriodo() { return periodo; }
    public int getVagas() { return vagas; }
    public int getVagasOcupadas() { return vagasOcupadas; } // REQUISITO TASK 2108
    public String getHorario() { return horario; }
    public String getSala() { return sala; }

    // Setters 
    public void setVagas(int vagas) { this.vagas = vagas; }
    public void setVagasOcupadas(int vagasOcupadas) { this.vagasOcupadas = vagasOcupadas; } // REQUISITO TASK 2108
    public void setHorario(String horario) { this.horario = horario; }
    public void setSala(String sala) { this.sala = sala; }

    @Override
    public String toString() {
        // Incluído o vagasOcupadas na serialização para salvar corretamente no arquivo txt do grupo
        return codigoDisciplina + ";" + matriculaProfessor + ";" + periodo + ";" + vagas + ";" + vagasOcupadas + ";" + horario + ";" + sala;
    }
}