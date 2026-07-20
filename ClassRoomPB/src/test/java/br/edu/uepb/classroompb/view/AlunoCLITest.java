package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.*;
import br.edu.uepb.classroompb.repository.*;
import br.edu.uepb.classroompb.service.*;
import java.util.ArrayList;
import org.junit.Test;

public class AlunoCLITest {
  @Test
  public void testProcessar() throws Exception {
    TurmaRepository tr = new TurmaRepository();
    PeriodoRepository pr = new PeriodoRepository();
    DisciplinaRepository dr = new DisciplinaRepository();
    MatriculaRepository mr = new MatriculaRepository();
    FrequenciaRepository fr = new FrequenciaRepository();
    NotaRepository nr = new NotaRepository();
    HistoricoRepository hr = new HistoricoRepository();

    pr.salvar(new Periodo("P01", "ATIVO"));
    dr.salvar(new Disciplina("D01", "Nome", 60, 4, new ArrayList<>()));

    ArrayList<String> preReq = new ArrayList<>();
    preReq.add("D01");
    dr.salvar(new Disciplina("D02", "Avancada", 60, 4, preReq));

    tr.salvar(new Turma("D01", "PR123", "P01", 40, "SEG", "S01"));
    tr.salvar(new Turma("D02", "PR123", "P01", 40, "TER", "S02"));

    TurmaService ts = new TurmaService(tr, pr, dr);
    MatriculaService ms = new MatriculaService(tr, mr, pr, dr, hr);
    FrequenciaService fs = new FrequenciaService(tr, mr, fr, nr);
    SituacaoAcademicaService sas = new SituacaoAcademicaService(nr, fs);
    HistoricoService hs = new HistoricoService(hr, mr, tr, sas, fs);

    AutenticacaoService auth = AutenticacaoService.getInstancia();
    try {
      auth.cadastrarUsuario("aluno", "Nome", "AL123", "al123@test", "senha", "C01");
    } catch (Exception e) {
    }
    auth.realizarLogin("AL123", "senha");

    AlunoCLI cli = new AlunoCLI(ts, ms, hs);

    cli.processar(null);
    cli.processar("   ");
    cli.processar("invalido");

    cli.processar("listarTurmas");

    // Deve falhar pois D02 precisa de D01
    cli.processar("solicitarMatricula D02 P01");

    // Insere aprovação manual no histórico
    hr.salvarLote(
        java.util.Collections.singletonList(
            new Historico("AL123", "P00", "D01", "PR123", 9.0, 100.0, StatusAcademico.APROVADO)));

    // Deve passar agora
    cli.processar("solicitarMatricula D02 P01");

    cli.processar("solicitarMatricula D01 P01");
    cli.processar("solicitarMatricula");

    cli.processar("cancelarMatricula D01 P01");
    cli.processar("cancelarMatricula");

    cli.processar("consultarFrequencia D01 P01");
    cli.processar("consultarFrequencia");

    nr.salvar(new br.edu.uepb.classroompb.model.Nota("AL123", "D01", "P01", 1, 8.5));
    nr.salvar(new br.edu.uepb.classroompb.model.Nota("AL123", "D01", "P01", 2, 7.5));

    java.util.List<br.edu.uepb.classroompb.model.Frequencia> frequencias =
        new java.util.ArrayList<>();
    frequencias.add(
        new br.edu.uepb.classroompb.model.Frequencia(
            "27/06/2026",
            "AL123",
            "D01",
            "P01",
            br.edu.uepb.classroompb.model.Frequencia.TipoFrequencia.PRESENCA));
    frequencias.add(
        new br.edu.uepb.classroompb.model.Frequencia(
            "28/06/2026",
            "AL123",
            "D01",
            "P01",
            br.edu.uepb.classroompb.model.Frequencia.TipoFrequencia.FALTA));
    fr.salvarLote(frequencias);

    // Agora as consultas trarão resultados não vazios
    cli.processar("consultarFrequencia D01 P01");

    cli.processar("consultarNotas P01");
    cli.processar("consultarNotas");

    cli.processar("consultarSituacao D01 P01");
    cli.processar("consultarSituacao");

    cli.processar("consultarHistorico");
  }
}
