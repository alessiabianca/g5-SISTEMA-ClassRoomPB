package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.exception.UsuarioJaExisteException;

public class AuthCLI {
    private final AutenticacaoService authService = AutenticacaoService.getInstancia();

    public void processar(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] tokens = input.trim().split("\\s+");
        String comando = tokens[0];

        try {
            if (comando.startsWith("cadastrar")) {
                if (tokens.length < 6) {
                    System.out.println("Erro: Argumentos insuficientes.");
                    System.out.println("Uso correto: " + comando + " [nome] [cpf] [matricula] [email] [senha]");
                    return;
                }

                String tipoPerfil = comando.replace("cadastrar", "").toLowerCase();
                
                String nome = tokens[1];
                String cpf = tokens[2];
                String matricula = tokens[3];
                String email = tokens[4];
                String senha = tokens[5];

                authService.cadastrarUsuario(tipoPerfil, nome, matricula, email, senha);
                System.out.println("Sucesso: Usuário cadastrado com êxito!");

            } else {
                System.out.println("Erro: Comando '" + comando + "' não reconhecido no Módulo de Autenticação.");
            }
        } catch (UsuarioJaExisteException e) {
            // Captura especificamente a nova exceção customizada da Task 1838
            System.out.println("[Conflito de Cadastro] " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}