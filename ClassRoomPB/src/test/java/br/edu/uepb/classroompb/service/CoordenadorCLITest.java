package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.view.CoordenadorCLI;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;

public class CoordenadorCLITest {

  private CoordenadorCLI coordenadorCLI;
  private final ByteArrayOutputStream outputError = new ByteArrayOutputStream();

  @Before
  public void setUp() {
    coordenadorCLI = new CoordenadorCLI();
    // Redireciona a saída de erro do sistema (System.err) para conseguirmos capturar no assert
    System.setErr(new PrintStream(outputError));
    // Garante o isolamento do teste limpando qualquer sessão ativa antes da execução
    AutenticacaoService.getInstancia().realizarLogout();
  }

  @Test
  public void deveBloquearOfertaDeTurmaQuandoNaoHouverUsuarioLogado() {
    // Executa o comando em um cenário onde o usuário logado é 'null'
    coordenadorCLI.processar("ofertarTurma ES01 PROF_123 2026.1 40 08:00-10:00 Sala_1");

    String resultado = outputError.toString();
    assertTrue(
        "O sistema deveria ter negado o acesso por falta de credenciais válidas.",
        resultado.contains(
            "ACESSO NEGADO: Apenas usuarios autenticados com o perfil de Coordenador"));
  }
}
