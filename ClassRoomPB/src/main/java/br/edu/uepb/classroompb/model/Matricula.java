package br.edu.uepb.classroompb.model;

public class Matricula {
    
    // Enum interno para controle estrito dos estados lógicos permitidos (US16 - RF20)
    public enum StatusMatricula {
        SOLICITADA,
        CONFIRMADA,
        REJEITADA,
        ESPERA
    }

    private String matriculaAluno;
    private String codigoDisciplina;
    private String periodo;
    private StatusMatricula status; // Tipo alterado de String para o Enum de controle

    // Construtor completo utilizando o Enum
    public Matricula(String matriculaAluno, String codigoDisciplina, String periodo, StatusMatricula status) {
        this.matriculaAluno = matriculaAluno;
        this.codigoDisciplina = codigoDisciplina;
        this.periodo = periodo;
        this.status = status;
    }

    // Getters e Setters adaptados
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

    public StatusMatricula getStatus() {
        return status;
    }

    /**
     * Mecanismo de Transição Limpa de Estado (US16)
     * Permite alterar o estado da matrícula de forma segura durante o pipeline.
     */
    public void transitarPara(StatusMatricula novoStatus) {
        if (novoStatus != null) {
            this.status = novoStatus;
        }
    }

    /**
     * Converte o objeto para o formato de persistência em arquivo plano.
     * O status.name() garante a gravação da String exata do Enum em maiúsculo.
     */
    @Override
    public String toString() {
        return matriculaAluno + ";" + codigoDisciplina + ";" + periodo + ";" + status.name();
    }
}