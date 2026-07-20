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

      // Cadastro de todos os tipos
      sb.append("2\nNome 1 1 email@aluno.com senha C01\n");
      sb.append("3\nNome 2 2 email@prof.com senha\n");
      sb.append("4\nNome 3 3 email@coord.com senha C01\n");
      sb.append("5\nNome 4 4 email@admin.com senha\n");

      // Login falho
      sb.append("1\nfalha@test.com senha\n");

      // Login Admin
      sb.append("1\nemail@admin.com senha\n");
      sb.append("invalido\n");
      sb.append("1\nP01\n"); // cadastrar periodo
      sb.append("2\nP01\n"); // ativar periodo
      sb.append("3\nP01\n"); // encerrar periodo
      sb.append("4\nC01\nCurso\n"); // cadastrar curso
      sb.append("5\n1\nC01\n"); // vincular curso
      sb.append("6\n"); // relatorio usuarios
      sb.append("7\n"); // logout

      // Login Coord
      sb.append("1\nemail@coord.com senha\n");
      sb.append("invalido\n");
      sb.append("1\nD01\nMatematica\n60\n4\nNENHUM\n");
      sb.append("2\nD01\n2\nP01\n40\nSEG\nS01\n"); // ofertar turma
      sb.append("3\nD01\nP01\nprofX\n50\nTER\nS02\n"); // editar turma
      sb.append("4\nD01\nP01\n"); // cancelar turma
      sb.append("5\nD01\nP01\n"); // lista espera
      sb.append("6\nD01\nP01\n"); // relatorio alunos matriculados
      sb.append("7\nP02\n"); // cadastrar periodo
      sb.append("8\nP02\n"); // ativar periodo
      sb.append("9\nP02\n"); // encerrar periodo
      sb.append("10\n1\n"); // historico
      sb.append("11\nP01\n"); // relatorio ocupacao
      sb.append("11\n\n"); // relatorio ocupacao sem periodo
      sb.append("12\nC01\n"); // relatorio reprovacao
      sb.append("12\n\n"); // relatorio reprovacao sem disciplina
      sb.append("13\n"); // logout

      // Login Professor
      sb.append("1\nemail@prof.com senha\n");
      sb.append("invalido\n");
      sb.append("1\nD01\nP01\n2026-01-01\n"); // registrar chamada
      sb.append("2\n1\nD01\n1\n10.0\n"); // lancar nota
      sb.append("3\n1\nD01\n1\n8.0\n"); // retificar
      sb.append("4\n"); // logout

      // Login Aluno
      sb.append("1\nemail@aluno.com senha\n");
      sb.append("invalido\n");
      sb.append("1\nD01\nP01\n"); // solicitar
      sb.append("2\nD01\nP01\n"); // cancelar
      sb.append("3\n"); // consultar historico
      sb.append("4\n"); // consultar turmas
      sb.append("5\nD01\nP01\n"); // consultar freq
      sb.append("6\nP01\n"); // consultar notas
      sb.append("7\nD01\nP01\n"); // situacao
      sb.append("8\n"); // logout

      sb.append("0\n"); // sair

      ByteArrayInputStream in = new ByteArrayInputStream(sb.toString().getBytes());
      System.setIn(in);

      TerminalCLI cli = new TerminalCLI();
      cli.iniciar();
    } catch (Exception e) {
      // Ignored
    } finally {
      System.setIn(sysInBackup);
    }
  }
}
