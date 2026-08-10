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
    DiarioRepository drp = new DiarioRepository();
    AulaRepository ar = new AulaRepository();

    PeriodoService ps =
        new PeriodoService(
            pr,
            new HistoricoService(
                hr,
                mr,
                tr,
                new SituacaoAcademicaService(
                    nr,
                    new FrequenciaService(tr, mr, fr, nr, drp, ar),
                    new AvaliacaoRepository(),
                    new DiarioRepository()),
                new FrequenciaService(tr, mr, fr, nr, drp, ar)));
    TurmaService ts = new TurmaService(tr, pr, dr);

    AdminCLI cli = new AdminCLI(ps, ts);

    // Invalid inputs
    cli.processar(null);
    cli.processar("   ");
    cli.processar("invalido");

    // Periodos
    cli.processar("cadastrarPeriodo"); // Falta arg
    cli.processar("cadastrarPeriodo 2026.3");
    cli.processar("cadastrarPeriodo 2026.3"); // Ja existe

    cli.processar("ativarPeriodo"); // Falta arg
    cli.processar("ativarPeriodo 2026.3");

    cli.processar("encerrarPeriodo"); // Falta arg
    cli.processar("encerrarPeriodo 2026.3");

    // Turmas
    cli.processar("ofertarTurma"); // Faltam args
    dr.salvar(
        new br.edu.uepb.classroompb.model.Disciplina(
            "DISC99", "Disc 99", 60, 4, new java.util.ArrayList<>()));
    pr.salvar(new br.edu.uepb.classroompb.model.Periodo("2026.4", "INICIADO"));
    cli.processar("ofertarTurma DISC99 PROF99 2026.4 40 08:00 SALA_99"); // Valido
    cli.processar("ofertarTurma DISC99 PROF99 2026.4 XXX 08:00 SALA_99"); // Vagas invalido

    // Relatorio
    cli.processar("gerarRelatorioGeralUsuariosCadastrados");
  }
}
