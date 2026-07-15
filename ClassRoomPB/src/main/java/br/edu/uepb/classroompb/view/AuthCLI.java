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
      // Interceptador de segurança para controle de comandos aceitos por perfil (Task 1714)
      if (authService.getUsuarioLogado() != null) {
        String perfilAtivo = authService.getUsuarioLogado().getPerfil();

        // Exemplo de barramento: Se for um Aluno tentando rodar comandos administrativos ou de
        // cadastro
        if (comando.startsWith("cadastrar") && "ALUNO".equalsIgnoreCase(perfilAtivo)) {
          System.out.println(
              "Erro: Acesso negado. O perfil '"
                  + perfilAtivo
                  + "' não tem permissão para cadastrar usuários.");
          return;
        }
      }

      if (comando.startsWith("cadastrar")) {
        if (tokens.length < 6) {
          System.out.println("Erro: Argumentos insuficientes.");
          System.out.println(
              "Uso correto: " + comando + " [nome] [cpf] [matricula] [email] [senha]");
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

      } else if (comando.equalsIgnoreCase("login")) {
        // Implementação do comando no formato: login email senha (Task 1714)
        if (tokens.length < 3) {
          System.out.println("Erro: Argumentos insuficientes.");
          System.out.println("Uso correto: login [email/matricula] [senha]");
          return;
        }

        String id = tokens[1];
        String senha = tokens[2];

        // Aciona o motor para validar as credenciais e injetar o usuário na sessão global
        authService.realizarLogin(id, senha);
        System.out.println("Sucesso: Login realizado com sucesso! Sessão ativa para o usuário.");

      } else if (comando.equalsIgnoreCase("logout")) {
        if (authService.getUsuarioLogado() == null) {
          System.out.println("Erro: Não há nenhuma sessão ativa no momento.");
          return;
        }
        authService.realizarLogout();
        System.out.println("Sucesso: Sessão encerrada.");

      } else {
        System.out.println(
            "Erro: Comando '" + comando + "' não reconhecido no Módulo de Autenticação.");
      }
    } catch (UsuarioJaExisteException e) {
      System.out.println("[Conflito de Cadastro] " + e.getMessage());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
