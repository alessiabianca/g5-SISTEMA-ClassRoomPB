package br.edu.uepb.classroompb.view;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.service.AutenticacaoService;

public class TerminalCLITest {
  
  @Before
  public void setUp() {
    File dir = new File("data");
    if (!dir.exists()) dir.mkdirs();
    new File("data/usuarios.json").delete();
    new File("data/disciplinas.txt").delete();
    new File("data/cursos.txt").delete();
    new File("data/periodos.txt").delete();
    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();
    new File("data/avaliacoes.txt").delete();
    new File("data/diarios.txt").delete();
    new File("data/frequencias.txt").delete();
    new File("data/notas.txt").delete();
    
    try {
      escreverNoArquivo("data/diarios.txt", "DIARIO1;D01;P01;Diario de D01;MAT2;08:00;S01;60;ABERTO");
      escreverNoArquivo("data/turmas.txt", "D01;P01;40;0");
      escreverNoArquivo("data/periodos.txt", "P01;ATIVO");
      escreverNoArquivo("data/disciplinas.txt", "D01;Matematica;60;4;NENHUM");
      escreverNoArquivo("data/matriculas.txt", "MAT1;D01;P01;CONFIRMADA");
      escreverNoArquivo("data/avaliacoes.txt", "AVAL1;DIARIO1;Prova 1;1;2.0;10.0");
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    AutenticacaoService.getInstancia().realizarLogout();
  }

  private void escreverNoArquivo(String caminho, String conteudo) throws IOException {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho, true))) {
      bw.write(conteudo);
      bw.newLine();
    }
  }

  private void runCliWithInput(String input) {
      InputStream sysInBackup = System.in;
      try {
          ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
          System.setIn(in);
          TerminalCLI cli = new TerminalCLI();
          cli.iniciar();
      } catch (Exception e) {
          // ignore stream end
      } finally {
          System.setIn(sysInBackup);
      }
  }

  @Test
  public void testVisitante() {
      StringBuilder sb = new StringBuilder();
      sb.append("comandoInvalido\n");
      // Cadastro Incompleto (Sem curso)
      sb.append("2\nAluno SemCurso\n111\nMAT_SEM\nalunosc@test.com\nsenha\n\n");
      // Cadastro Aluno
      sb.append("2\nAluno Silva\n111\nMAT1\naluno@test.com\nsenha\nC01\n");
      // Cadastro Prof
      sb.append("3\nProf Silva\n222\nMAT2\nprof@test.com\nsenha\n");
      // Cadastro Coord
      sb.append("4\nCoord Silva\n333\nMAT3\ncoord@test.com\nsenha\nC01\n");
      // Cadastro Admin
      sb.append("5\nAdmin Silva\n444\nMAT4\nadmin@test.com\nsenha\n");
      // Login falha
      sb.append("1\nfalha@test.com\nsenha\n");
      sb.append("0\n");
      
      runCliWithInput(sb.toString());
  }
  
  @Test
  public void testAdmin() {
      try {
          AutenticacaoService.getInstancia().cadastrarUsuario("administrador", "Admin Silva", "444", "admin@test.com", "senha", null);
      } catch(Exception e) {}
      
      StringBuilder sb = new StringBuilder();
      sb.append("1\nadmin@test.com\nsenha\n");
      sb.append("invalido\n");
      sb.append("1\nP02\n");
      sb.append("2\nP02\n");
      sb.append("3\nP02\n");
      sb.append("4\nC02\nComputacao\n");
      sb.append("5\nMAT1\nC02\n");
      sb.append("6\n");
      sb.append("7\n"); // Logout
      sb.append("0\n");
      
      runCliWithInput(sb.toString());
  }

  @Test
  public void testCoordenador() {
      try {
          AutenticacaoService.getInstancia().cadastrarUsuario("coordenador", "Coord Silva", "333", "coord@test.com", "senha", "C01");
      } catch(Exception e) {}
      
      StringBuilder sb = new StringBuilder();
      sb.append("1\ncoord@test.com\nsenha\n");
      sb.append("invalido\n");
      sb.append("1\nD02\nMatematica\n60\n4\n\n");
      sb.append("1\nD03\nFisica\nAB\n4\n\n"); // Erro NumberFormat
      sb.append("2\nD02\nMAT2\nP01\n40\n08:00\nS01\n");
      sb.append("3\nD02\nP01\nMAT2\n50\n10:00\nS02\n");
      sb.append("4\nD02\nP01\n");
      sb.append("5\nD01\nP01\n");
      sb.append("6\nD01\nP01\n");
      sb.append("7\nP03\n");
      sb.append("8\nP03\n");
      sb.append("9\nP03\n");
      sb.append("10\nMAT1\n");
      sb.append("11\nP01\n");
      sb.append("11\n\n");
      sb.append("12\nD01\n");
      sb.append("12\n\n");
      sb.append("13\n"); // Logout
      sb.append("0\n");
      
      runCliWithInput(sb.toString());
  }
  
  @Test
  public void testProfessor() {
      try {
          AutenticacaoService.getInstancia().cadastrarUsuario("professor", "Prof Silva", "222", "prof@test.com", "senha", null);
      } catch(Exception e) {}
      
      StringBuilder sb = new StringBuilder();
      sb.append("1\nprof@test.com\nsenha\n");
      sb.append("invalido\n");
      sb.append("1\nDIARIO1\nAULA1\n");
      sb.append("P\n"); // Presença MAT1
      sb.append("2\nMAT1\nAVAL1\n10.0\n");
      sb.append("3\nMAT1\nAVAL1\n8.5\n");
      sb.append("4\nDIARIO1\nProva2\n2\n2.0\n10.0\n");
      sb.append("5\n"); // Logout
      sb.append("0\n");
      
      runCliWithInput(sb.toString());
  }

  @Test
  public void testAluno() {
      try {
          AutenticacaoService.getInstancia().cadastrarUsuario("aluno", "Aluno Silva", "111", "aluno@test.com", "senha", "C01");
      } catch(Exception e) {}
      
      StringBuilder sb = new StringBuilder();
      sb.append("1\naluno@test.com\nsenha\n");
      sb.append("invalido\n");
      sb.append("1\nD02\nP01\n");
      sb.append("2\nD02\nP01\n");
      sb.append("3\n");
      sb.append("4\n");
      sb.append("5\nD01\nP01\n");
      sb.append("6\nP01\n");
      sb.append("7\nD01\nP01\n");
      sb.append("8\n"); // Logout
      sb.append("0\n");
      
      runCliWithInput(sb.toString());
  }

  @Test
  public void testPerfilDesconhecido() {
      try {
          AutenticacaoService.getInstancia().cadastrarUsuario("misterio", "Misterio", "M999", "misterio@test.com", "senha", null);
      } catch(Exception e) {}
      
      StringBuilder sb = new StringBuilder();
      sb.append("1\nmisterio@test.com\nsenha\n");
      sb.append("1\n"); // Tentar logout para perfil em dev
      sb.append("0\n");
      
      runCliWithInput(sb.toString());
  }
}
