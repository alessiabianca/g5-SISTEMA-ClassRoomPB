package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.service.AutenticacaoService;

public class AuthCLI {
    private final AutenticacaoService authService = AutenticacaoService.getInstancia();

    public void processar(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        // Divide o comando tratando múltiplos espaços consecutivos
        String[] tokens = input.trim().split("\\s+");
        String comando = tokens[0];

        try {
            if (comando.startsWith("cadastrar")) {
                // Validação do formato esperado: cadastrar[Perfil] nome cpf matricula email senha
                if (tokens.length < 6) {
                    System.out.println("Erro: Argumentos insuficientes.");
                    System.out.println("Uso correto: " + comando + " [nome] [cpf] [matricula] [email] [senha]");
                    return;
                }

                // Extrai o tipo de perfil diretamente do nome do comando (ex: "cadastrarAluno" -> "aluno")
                String tipoPerfil = comando.replace("cadastrar", "").toLowerCase();
                
                String nome = tokens[1];
                String cpf = tokens[2];
                String matricula = tokens[3];
                String email = tokens[4];
                String senha = tokens[5];

                // Como a assinatura padrão da US01 gerencia via tipoPerfil, repassamos os parâmetros
                // O CPF pode ser validado internamente ou associado ao fluxo do Service futuramente.
                authService.cadastrarUsuario(tipoPerfil, nome, matricula, email, senha);
                System.out.println("Sucesso: Usuário cadastrado com êxito!");

            } else {
                System.out.println("Erro: Comando '" + comando + "' não reconhecido no Módulo de Autenticação.");
            }
        } catch (Exception e) {
            // Exibe a mensagem amigável capturada das regras de negócio (ex: bloqueio de duplicatas)
            System.out.println(e.getMessage());
        }
    }
}