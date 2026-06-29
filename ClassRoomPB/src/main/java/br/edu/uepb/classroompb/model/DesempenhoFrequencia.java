package br.edu.uepb.classroompb.model;

public class DesempenhoFrequencia {
    private int totalAulas;
    private int presencas;
    private int faltas;
    private double percentualFrequencia;

    public DesempenhoFrequencia(int totalAulas, int presencas, int faltas, double percentualFrequencia) {
        this.totalAulas = totalAulas;
        this.presencas = presencas;
        this.faltas = faltas;
        this.percentualFrequencia = percentualFrequencia;
    }

    public int getTotalAulas() { return totalAulas; }
    public int getPresencas() { return presencas; }
    public int getFaltas() { return faltas; }
    public double getPercentualFrequencia() { return percentualFrequencia; }
}