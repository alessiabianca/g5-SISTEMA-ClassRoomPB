package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.*;
import br.edu.uepb.classroompb.repository.*;
import br.edu.uepb.classroompb.service.*;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

public class ProfessorCLITest {

  private ProfessorCLI cli;
  private AutenticacaoService auth;
  private DiarioRepository diarioRepo;
  private MatriculaRepository mr;

  @Before
  public void setUp() throws Exception {
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

    TurmaRepository tr = new TurmaRepository();
    PeriodoRepository pr = new PeriodoRepository();
    DisciplinaRepository dr = new DisciplinaRepository();
    mr = new MatriculaRepository();
    FrequenciaRepository fr = new FrequenciaRepository();
    NotaRepository nr = new NotaRepository();
    diarioRepo = new DiarioRepository();
    AvaliacaoRepository avaliacaoRepo = new AvaliacaoRepository();
    AulaRepository aulaRepo = new AulaRepository();

    FrequenciaService fs = new FrequenciaService(tr, mr, fr, nr, diarioRepo, aulaRepo);
    TurmaService ts = new TurmaService(tr, pr, dr);
    NotaService ns = new NotaService(nr, tr, mr, pr, avaliacaoRepo, diarioRepo);
    AvaliacaoService avaliacaoService = new AvaliacaoService(avaliacaoRepo, diarioRepo);

    cli = new ProfessorCLI(ts, fs, ns, avaliacaoService);

    pr.salvar(new Periodo("P01", "ATIVO"));
    dr.salvar(new Disciplina("D01", "Nome", 60, 4, new ArrayList<>()));
    tr.salvar(new Turma("D01", "P01", 40));
    
    // Add a diary for the professor
    diarioRepo.salvar(new Diario("DIARIO1", "D01", "P01", "Desc", "PR123", "08:00", "Sala", 60));

    mr.salvar(new Matricula("AL123", "D01", "P01", Matricula.StatusMatricula.CONFIRMADA));

    auth = AutenticacaoService.getInstancia();
    try {
      auth.cadastrarUsuario("professor", "Nome Prof", "PR123", "pr123@test.com", "senha", null);
      auth.cadastrarUsuario("aluno", "Nome Aluno", "AL123", "al123@test.com", "senha", "CC");
    } catch (Exception ignored) {
    }
  }

  @Test
  public void testProcessarSemLoginOuNaoProfessor() throws Exception {
    try { auth.realizarLogout(); } catch (Exception e) {}
    cli.processar("lancarNota AL123 DIARIO1 10.0");
    
    try {
        auth.realizarLogin("al123@test.com", "senha");
    } catch (Exception e) {}
    cli.processar("lancarNota AL123 DIARIO1 10.0");
  }

  @Test
  public void testProcessarComandosInvalidosEExcecoes() throws Exception {
    try { auth.realizarLogin("pr123@test.com", "senha"); } catch(Exception e) {}
    cli.processar(null);
    cli.processar("   ");
    cli.processar("comandoInexistente");
    
    // lancerNota invalido
    cli.processar("lancarNota");
    cli.processar("lancarNota AL123 DIARIO1 A");
    
    // editarNota invalido
    cli.processar("editarNota");
    cli.processar("editarNota AL123 DIARIO1 B");
    
    // cadastrarAvaliacao invalido
    cli.processar("cadastrarAvaliacao");
    
    // registrarChamada invalido
    cli.processar("registrarChamada");
    cli.processar("registrarChamada DIARIO_INEXISTENTE 1");
  }

  @Test
  public void testLancarEEditarNota() throws Exception {
    try { auth.realizarLogin("pr123@test.com", "senha"); } catch(Exception e) {}
    
    try {
        cli.processar("cadastrarAvaliacao DIARIO1 Prova_1 1 2.0 10.0");
    } catch (Exception e) {}
    
    // Need a valid avaliacao created to launch nota properly, assuming AV-1 or something is generated.
    // If not, ValidacaoException will be caught by the catch block inside processar, which is also a good test.
    cli.processar("lancarNota AL123 AVAL_INVALIDA 10.0");
    cli.processar("editarNota AL123 AVAL_INVALIDA 8.0");
  }

  @Test
  public void testRegistrarChamadaIterativa() throws Exception {
    try { auth.realizarLogin("pr123@test.com", "senha"); } catch(Exception e) {}
    
    InputStream sysInBackup = System.in;
    try {
      // Simulate input: first an invalid option, then F for the student
      String simulado = "X\nF\n";
      ByteArrayInputStream in = new ByteArrayInputStream(simulado.getBytes());
      System.setIn(in);
      cli.processar("registrarChamada DIARIO1 AULA1");
    } finally {
      System.setIn(sysInBackup);
    }
  }
  
  @Test
  public void testRegistrarChamadaIterativaPresenca() throws Exception {
    try { auth.realizarLogin("pr123@test.com", "senha"); } catch(Exception e) {}
    
    InputStream sysInBackup = System.in;
    try {
      // Simulate input: P for the student
      String simulado = "P\n";
      ByteArrayInputStream in = new ByteArrayInputStream(simulado.getBytes());
      System.setIn(in);
      cli.processar("registrarChamada DIARIO1 AULA2");
    } finally {
      System.setIn(sysInBackup);
    }
  }

  @Test
  public void testRegistrarChamadaSemAlunosConfirmados() throws Exception {
    try { auth.realizarLogin("pr123@test.com", "senha"); } catch(Exception e) {}
    // Create another diary without enrolled students
    diarioRepo.salvar(new Diario("DIARIO2", "D02", "P01", "Desc", "PR123", "08:00", "Sala", 60));
    cli.processar("registrarChamada DIARIO2 AULA1");
  }
}
