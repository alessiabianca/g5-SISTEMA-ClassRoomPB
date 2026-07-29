package br.edu.uepb.classroompb.view;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;

public class TerminalCLITest {
  @Test
  public void testTerminalCLICompleto() {
    InputStream sysInBackup = System.in;
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("comandoInvalido\n");

      sb.append("2\nNome 1 1 email@aluno.com senha C01\n");
      sb.append("3\nNome 2 2 email@prof.com senha\n");
      sb.append("4\nNome 3 3 email@coord.com senha C01\n");
      sb.append("5\nNome 4 4 email@admin.com senha\n");

      sb.append("1\nfalha@test.com senha\n");

      sb.append("1\nemail@admin.com senha\n");
      sb.append("invalido\n");
      sb.append("1\nP01\n");
      sb.append("2\nP01\n");
      sb.append("3\nP01\n");
      sb.append("4\nC01\nCurso\n");
      sb.append("5\n1\nC01\n");
      sb.append("6\n");
      sb.append("7\n");

      sb.append("1\nemail@coord.com senha\n");
      sb.append("invalido\n");
      sb.append("1\nD01\nMatematica\n60\n4\nNENHUM\n");
      sb.append("2\nD01\n2\nP01\n40\nSEG\nS01\n");
      sb.append("3\nD01\nP01\nprofX\n50\nTER\nS02\n");
      sb.append("4\nD01\nP01\n");
      sb.append("5\nD01\nP01\n");
      sb.append("6\nD01\nP01\n");
      sb.append("7\nP02\n");
      sb.append("8\nP02\n");
      sb.append("9\nP02\n");
      sb.append("10\n1\n");
      sb.append("11\nP01\n");
      sb.append("11\n\n");
      sb.append("12\nC01\n");
      sb.append("12\n\n");
      sb.append("13\n");

      sb.append("1\nemail@prof.com senha\n");
      sb.append("invalido\n");
      sb.append("1\nD01\nP01\n2026-01-01\n");
      sb.append("2\n1\nD01\n1\n10.0\n");
      sb.append("3\n1\nD01\n1\n8.0\n");
      sb.append("4\n");

      sb.append("1\nemail@aluno.com senha\n");
      sb.append("invalido\n");
      sb.append("1\nD01\nP01\n");
      sb.append("2\nD01\nP01\n");
      sb.append("3\n");
      sb.append("4\n");
      sb.append("5\nD01\nP01\n");
      sb.append("6\nP01\n");
      sb.append("7\nD01\nP01\n");
      sb.append("8\n");

      sb.append("0\n");

      ByteArrayInputStream in = new ByteArrayInputStream(sb.toString().getBytes());
      System.setIn(in);

      TerminalCLI cli = new TerminalCLI();
      cli.iniciar();
    } catch (Exception e) {
    } finally {
      System.setIn(sysInBackup);
    }
  }
}
