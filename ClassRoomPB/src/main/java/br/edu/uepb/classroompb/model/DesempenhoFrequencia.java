package br.edu.uepb.classroompb.model;

 // Encapsula os dados consolidados do desempenho de assiduidade de um estudante.
 
public class DesempenhoFrequencia {
    private final int totalAulas;
    private final int presencas;
    private final int faltas;
    private final double percentualFrequencia;

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