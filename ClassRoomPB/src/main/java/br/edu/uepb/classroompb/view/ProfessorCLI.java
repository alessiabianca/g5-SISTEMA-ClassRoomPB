package br.edu.uepb.classroompb.view;

public class ProfessorCLI {
    public void processar(String input) {
        String comando = input.split(" ")[0];
        System.out.println("[ProfessorCLI] Processando comando de professor: " + comando);
        // Implementar lógica de chamada aos Services de Professor (Notas, Frequencia)
    }
}
