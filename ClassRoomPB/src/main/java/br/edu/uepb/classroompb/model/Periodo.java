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

    @Override
    public String toString() {
        return codigo + ";" + status;
    }
}