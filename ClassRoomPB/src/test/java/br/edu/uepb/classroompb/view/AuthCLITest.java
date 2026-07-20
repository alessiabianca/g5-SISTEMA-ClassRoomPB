package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.service.AutenticacaoService;
import org.junit.Test;

public class AuthCLITest {
  @Test
  public void testProcessar() {
    AuthCLI authCLI = new AuthCLI();
    AutenticacaoService authService = AutenticacaoService.getInstancia();

    authCLI.processar(null);
    authCLI.processar("   ");

    authCLI.processar("cadastrarAluno");

    authCLI.processar("cadastrarAluno Nome 12345 55555 email5@test.com senha C01");

    authCLI.processar("cadastrarAluno Nome 12345 55555 email5@test.com senha C01");

    authCLI.processar("login");
    authCLI.processar("login invalido senha");

    authCLI.processar("login 55555 senha");

    authCLI.processar("cadastrarProfessor Nome 111 222 email2@test.com senha");

    authCLI.processar("logout");

    authCLI.processar("logout");

    authCLI.processar("vincularCursoUsuario 55555 C01");
    authCLI.processar("vincularCursoUsuario 55555");

    authCLI.processar("comandoInvalido");
  }
}
