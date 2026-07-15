package br.edu.uepb.classroompb.model;

public class DesempenhoFrequencia {
    private int totalAulas;
    private int presencas;
    private int faltas;
    private double percentualFrequencia;
    
    // Atributos adicionais de desempenho conforme requisitos
    private double notaEtapa1;
    private double notaEtapa2;

    // Construtor original mantido para não quebrar compatibilidades antigas
    public DesempenhoFrequencia(int totalAulas, int presencas, int faltas, double percentualFrequencia) {
        this.totalAulas = totalAulas;
        this.presencas = presencas;
        this.faltas = faltas;
        this.percentualFrequencia = percentualFrequencia;
        this.notaEtapa1 = 0.0;
        this.notaEtapa2 = 0.0;
    }

    // Novo construtor completo que engloba as frequências e as notas lançadas
    public DesempenhoFrequencia(int totalAulas, int presencas, int faltas, double percentualFrequencia, double notaEtapa1, double notaEtapa2) {
        this.totalAulas = totalAulas;
        this.presencas = presencas;
        this.faltas = faltas;
        this.percentualFrequencia = percentualFrequencia;
        this.notaEtapa1 = notaEtapa1;
        this.notaEtapa2 = notaEtapa2;
    }

    public int getTotalAulas() { return totalAulas; }
    public int getPresencas() { return presencas; }
    public int getFaltas() { return faltas; }
    public double getPercentualFrequencia() { return percentualFrequencia; }
    
    public double getNotaEtapa1() { return notaEtapa1; }
    public double getNotaEtapa2() { return notaEtapa2; }
}