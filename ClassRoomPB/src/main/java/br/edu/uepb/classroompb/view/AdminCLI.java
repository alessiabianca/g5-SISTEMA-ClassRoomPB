package br.edu.uepb.classroompb.view;

public class AdminCLI {
    public void processar(String input) {
        String comando = input.split(" ")[0];
        System.out.println("[AdminCLI] Processando comando de administrador: " + comando);
        // Implementar lógica de chamada aos Services de Admin
    }
}
