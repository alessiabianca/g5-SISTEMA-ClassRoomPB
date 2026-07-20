package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.service.AutenticacaoService;
import org.junit.Test;

public class AuthCLITest {
  @Test
  public void testProcessar() {
    AuthCLI authCLI = new AuthCLI();
    AutenticacaoService authService = AutenticacaoService.getInstancia();

    // Test null/empty
    authCLI.processar(null);
    authCLI.processar("   ");

    // Test cadastrar sem args
    authCLI.processar("cadastrarAluno");

    // Test cadastrar sucesso
    authCLI.processar("cadastrarAluno Nome 12345 55555 email5@test.com senha C01");

    // Test cadastrar duplicado
    authCLI.processar("cadastrarAluno Nome 12345 55555 email5@test.com senha C01");

    // Test login invalido
    authCLI.processar("login");
    authCLI.processar("login invalido senha");

    // Test login sucesso
    authCLI.processar("login 55555 senha");

    // Test permissao negada (aluno tentar cadastrar)
    authCLI.processar("cadastrarProfessor Nome 111 222 email2@test.com senha");

    // Logout
    authCLI.processar("logout");

    // Logout quando ja deslogado
    authCLI.processar("logout");

    // Vincular curso
    authCLI.processar("vincularCursoUsuario 55555 C01");
    authCLI.processar("vincularCursoUsuario 55555");

    // Comando invalido
    authCLI.processar("comandoInvalido");
  }
}
