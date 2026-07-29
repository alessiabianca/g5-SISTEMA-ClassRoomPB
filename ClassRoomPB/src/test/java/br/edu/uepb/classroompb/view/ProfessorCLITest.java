package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.*;
import br.edu.uepb.classroompb.repository.*;
import br.edu.uepb.classroompb.service.*;
import java.util.ArrayList;
import org.junit.Test;

public class ProfessorCLITest {
  @Test
  public void testProcessar() throws Exception {
    TurmaRepository tr = new TurmaRepository();
    PeriodoRepository pr = new PeriodoRepository();
    DisciplinaRepository dr = new DisciplinaRepository();
    MatriculaRepository mr = new MatriculaRepository();
    FrequenciaRepository fr = new FrequenciaRepository();
    NotaRepository nr = new NotaRepository();

    pr.salvar(new Periodo("P01", "ATIVO"));
    dr.salvar(new Disciplina("D01", "Nome", 60, 4, new ArrayList<>()));
    tr.salvar(new Turma("D01", "P01", 40));

    mr.salvar(
        new br.edu.uepb.classroompb.model.Matricula(
            "AL123",
            "D01",
            "P01",
            br.edu.uepb.classroompb.model.Matricula.StatusMatricula.CONFIRMADA));

    TurmaService ts = new TurmaService(tr, pr, dr);
    FrequenciaService fs = new FrequenciaService(tr, mr, fr, nr);
    NotaService ns = new NotaService(nr, tr, mr, pr);

    AutenticacaoService auth = AutenticacaoService.getInstancia();
    try {
      auth.cadastrarUsuario("professor", "Nome", "PR123", "pr123@test", "senha", null);
    } catch (Exception e) {
    }
    auth.realizarLogin("PR123", "senha");

    ProfessorCLI cli = new ProfessorCLI(ts, fs, ns);

    cli.processar(null);
    cli.processar("  ");
    cli.processar("invalido");

    cli.processar("visualizarTurmas");

    java.io.InputStream originalIn = System.in;
    try {
      java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream("P\n".getBytes());
      System.setIn(in);
      cli.processar("registrarChamada D01 P01 2026-01-01");
    } finally {
      System.setIn(originalIn);
    }

    cli.processar("registrarChamada D01 P01");

    cli.processar("lancarNota AL123 D01 1 10.0");
    cli.processar("lancarNota");

    cli.processar("retificarNota AL123 D01 1 8.0");
    cli.processar("retificarNota");

    cli.processar("acompanharAlunos D01 P01");
    cli.processar("acompanharAlunos");
  }
}
