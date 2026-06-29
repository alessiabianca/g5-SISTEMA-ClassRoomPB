package br.edu.uepb.classroompb.model;

public class Periodo {
    private String codigo; // Ex: "2026.1"
    private String status; // Ex: "PLANEJADO", "INICIADO", "ENCERRADO"

    public Periodo(String codigo, String status) {
        this.codigo = codigo;
        this.status = status;
    }

    public String getCodigo() { return codigo; }
    public String getStatus() { return status; }

    /**
     * Logica de controle de estados da Task 1834.
     * Define se o periodo letivo esta apto a receber novas matriculas de alunos.
     * @return true se o status for INICIADO, false caso contrario.
     */
    public boolean isAbertoParaMatriculas() {
        return "INICIADO".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return codigo + ";" + status;
    }
}