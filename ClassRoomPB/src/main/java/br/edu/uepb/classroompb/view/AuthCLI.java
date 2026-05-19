package br.edu.uepb.classroompb.view;

public class AuthCLI {
    public void processar(String input) {
        String comando = input.split(" ")[0];
        System.out.println("[AuthCLI] Processando comando de autenticação/cadastro: " + comando);
        // Implementar lógica de chamada aos Services de Auth
    }
}
