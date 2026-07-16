package br.edu.uepb.classroompb.model;

public class Historico {
    private String matriculaAluno;
    private String codigoDisciplina;
    private String periodo;
    private double notaFinal;
    private double frequencia;
    private StatusAcademico status;

    public Historico(String matriculaAluno, String codigoDisciplina, String periodo, double notaFinal, double frequencia, StatusAcademico status) {
        this.matriculaAluno = matriculaAluno;
        this.codigoDisciplina = codigoDisciplina;
        this.periodo = periodo;
        this.notaFinal = notaFinal;
        this.frequencia = frequencia;
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

    public double getMediaFinal() {
        return notaFinal;
    }

    public void setMediaFinal(double mediaFinal) {
        this.notaFinal = mediaFinal;
    }

    public double getNotaFinal() {
        return notaFinal;
    }

    public void setNotaFinal(double notaFinal) {
        this.notaFinal = notaFinal;
    }

    public double getPercentualFrequencia() {
        return frequencia;
    }

    public void setPercentualFrequencia(double percentualFrequencia) {
        this.frequencia = percentualFrequencia;
    }

    public double getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(double frequencia) {
        this.frequencia = frequencia;
    }

    public StatusAcademico getStatus() {
        return status;
    }

    public void setStatus(StatusAcademico status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return matriculaAluno + ";" + codigoDisciplina + ";" + periodo + ";" + notaFinal + ";" + frequencia + ";" + status.name();
    }

    public static Historico fromString(String linha) {
        String[] partes = linha.split(";");
        if (partes.length != 6) {
            throw new IllegalArgumentException("Registro de historico invalido: " + linha);
        }

        return new Historico(
                partes[0],
                partes[1],
                partes[2],
                Double.parseDouble(partes[3]),
                Double.parseDouble(partes[4]),
                StatusAcademico.valueOf(partes[5])
        );
    }
}
