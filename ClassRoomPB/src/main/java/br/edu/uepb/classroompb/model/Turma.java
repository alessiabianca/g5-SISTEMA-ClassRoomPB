package br.edu.uepb.classroompb.model;

public class Turma {
    private String codigoDisciplina;
    private String matriculaProfessor;
    private String periodo;
    private int vagas;
    private String horario;
    private String sala;

    public Turma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, String horario, String sala) {
        this.codigoDisciplina = codigoDisciplina;
        this.matriculaProfessor = matriculaProfessor;
        this.periodo = periodo;
        this.vagas = vagas;
        this.horario = horario;
        this.sala = sala;
    }

    public String getCodigoDisciplina() { return codigoDisciplina; }
    public String getMatriculaProfessor() { return matriculaProfessor; }
    public String getPeriodo() { return periodo; }
    public int getVagas() { return vagas; }
    public String getHorario() { return horario; }
    public String getSala() { return sala; }

    @Override
    public String toString() {
        return codigoDisciplina + ";" + matriculaProfessor + ";" + periodo + ";" + vagas + ";" + horario + ";" + sala;
    }
}