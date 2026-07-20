package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.repository.*;
import br.edu.uepb.classroompb.service.*;
import org.junit.Test;

public class AdminCLITest {
  @Test
  public void testProcessar() throws Exception {
    PeriodoRepository pr = new PeriodoRepository();
    TurmaRepository tr = new TurmaRepository();
    DisciplinaRepository dr = new DisciplinaRepository();
    HistoricoRepository hr = new HistoricoRepository();
    MatriculaRepository mr = new MatriculaRepository();
    NotaRepository nr = new NotaRepository();
    FrequenciaRepository fr = new FrequenciaRepository();

    PeriodoService ps =
        new PeriodoService(
            pr,
            new HistoricoService(
                hr,
                mr,
                tr,
                new SituacaoAcademicaService(nr, new FrequenciaService(tr, mr, fr, nr)),
                new FrequenciaService(tr, mr, fr, nr)));
    TurmaService ts = new TurmaService(tr, pr, dr);
    AutenticacaoService auth = AutenticacaoService.getInstancia();
    try {
      auth.cadastrarUsuario("administrador", "Nome", "AD123", "ad123@test", "senha", null);
    } catch (Exception e) {
    }
    auth.realizarLogin("AD123", "senha");

    AdminCLI cli = new AdminCLI(ps, ts);

    cli.processar(null);
    cli.processar("   ");
    cli.processar("invalido");
    cli.processar("cadastrarCurso");
    cli.processar("cadastrarCurso C01 Nome 100");
    cli.processar("gerarRelatorioGeralUsuariosCadastrados");
  }
}
