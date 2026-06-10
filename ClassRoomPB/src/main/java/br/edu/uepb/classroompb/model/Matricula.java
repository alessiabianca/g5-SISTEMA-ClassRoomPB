package br.edu.uepb.classroompb.model;

public class Matricula {
    private String matriculaAluno;
    private String codigoDisciplina;
    private String periodo;
    private String status; // Ex: "SOLICITADA", "CONFIRMADA", "REJEITADA"

    public Matricula(String matriculaAluno, String codigoDisciplina, String periodo, String status) {
        this.matriculaAluno = matriculaAluno;
        this.codigoDisciplina = codigoDisciplina;
        this.periodo = periodo;
        this.status = status;
    }

    public String getMatriculaAluno() {
        return matriculaAluno;
    }

    public void setMatriculaAluno(String matriculaAluno) {
        this.matriculaAluno = matriculaAluno;
    }

    public String getCodigoDisciplina() {
        return codigoDisciplina;
    }

    public void setCodigoDisciplina(String codigoDisciplina) {
        this.codigoDisciplina = codigoDisciplina;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Converte o objeto para o formato de persistência em arquivo plano.
     * Mantém o padrão CSV/TSV utilizado nos outros repositórios.
     */
    @Override
    public String toString() {
        return matriculaAluno + ";" + codigoDisciplina + ";" + periodo + ";" + status;
    }
}