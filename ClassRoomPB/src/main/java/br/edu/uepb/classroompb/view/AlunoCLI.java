package br.edu.uepb.classroompb.view;

public class AlunoCLI {
    public void processar(String input) {
        String comando = input.split(" ")[0];
        System.out.println("[AlunoCLI] Processando comando de aluno: " + comando);
        // Implementar lógica de chamada aos Services de Aluno (Matricula, Historico)
    }
}
