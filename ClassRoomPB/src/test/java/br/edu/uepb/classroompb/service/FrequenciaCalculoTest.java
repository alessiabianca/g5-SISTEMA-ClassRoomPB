package br.edu.uepb.classroompb.service;

import static org.junit.Assert.assertEquals;

import br.edu.uepb.classroompb.model.DesempenhoFrequencia;
import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FrequenciaCalculoTest {

  private FrequenciaService frequenciaService;
  private TurmaRepository turmaRepository;
  private MatriculaRepository matriculaRepository;
  private FrequenciaRepository frequenciaRepository;
  private NotaRepository notaRepository;

  private static final String FILE_TURMAS = "data/turmas.txt";
  private static final String FILE_FREQUENCIAS = "data/frequencias.txt";

  @Before
  public void setUp() throws Exception {
    File fTurmas = new File(FILE_TURMAS);
    if (fTurmas.exists()) fTurmas.delete();
    File fFrequencias = new File(FILE_FREQUENCIAS);
    if (fFrequencias.exists()) fFrequencias.delete();

    turmaRepository = new TurmaRepository();
    matriculaRepository = new MatriculaRepository();
    frequenciaRepository = new FrequenciaRepository();
    notaRepository = new NotaRepository();
    frequenciaService =
        new FrequenciaService(
            turmaRepository, matriculaRepository, frequenciaRepository, notaRepository);
  }

  @Test
  public void deveRetornarCemPorCentoQuandoNaoHouverNenhumaAulaRegistrada() throws Exception {
    turmaRepository.salvar(new Turma("ES01", "PROF_42", "2026.1", 40, "24M12", "Sala 1"));

    DesempenhoFrequencia resultado =
        frequenciaService.calcularPercentualFrequencia("2026100", "ES01", "2026.1");

    assertEquals(0, resultado.getTotalAulas());
    assertEquals(100.0, resultado.getPercentualFrequencia(), 0.01);
  }

  @Test
  public void deveCalcularPercentualExatoComFaltasAcumuladas() throws Exception {
    String aluno = "2026100";
    String disciplina = "ES01";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, "PROF_42", periodo, 40, "24M12", "Sala 1"));

    List<Frequencia> aulas = new ArrayList<>();
    aulas.add(
        new Frequencia(
            "01/06/2026", aluno, disciplina, periodo, Frequencia.TipoFrequencia.PRESENCA));
    aulas.add(
        new Frequencia(
            "03/06/2026", aluno, disciplina, periodo, Frequencia.TipoFrequencia.PRESENCA));
    aulas.add(
        new Frequencia(
            "05/06/2026", aluno, disciplina, periodo, Frequencia.TipoFrequencia.PRESENCA));
    aulas.add(
        new Frequencia("08/06/2026", aluno, disciplina, periodo, Frequencia.TipoFrequencia.FALTA));
    frequenciaRepository.salvarLote(aulas);

    DesempenhoFrequencia resultado =
        frequenciaService.calcularPercentualFrequencia(aluno, disciplina, periodo);

    assertEquals(4, resultado.getTotalAulas());
    assertEquals(3, resultado.getPresencas());
    assertEquals(1, resultado.getFaltas());
    assertEquals(75.0, resultado.getPercentualFrequencia(), 0.01);
  }

  /**
   * TESTE 1: Garante que a média aritmética é calculada de forma perfeita com notas decimais
   * fracionadas (ex: 7.5 e 8.5 resultando em média 8.0).
   */
  @Test
  public void deveCalcularMediaArithmeticaPerfeitamenteParaNotasFracionadas() throws Exception {
    String aluno = "20261101";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, "PROF_A", periodo, 30, "24M12", "Sala_101"));
    notaRepository.salvar(new Nota(aluno, disciplina, periodo, 7.5, 8.5, -1.0));

    DesempenhoFrequencia df =
        frequenciaService.calcularPercentualFrequencia(aluno, disciplina, periodo);

    assertEquals(7.5, df.getNotaEtapa1(), 0.01);
    assertEquals(8.5, df.getNotaEtapa2(), 0.01);

    double mediaCalculada = (df.getNotaEtapa1() + df.getNotaEtapa2()) / 2.0;
    assertEquals(8.0, mediaCalculada, 0.01);
  }

  /**
   * TESTE 2: Garante que se nenhuma nota foi lançada para o aluno ainda, o sistema atribui 0.0 como
   * valor padrão seguro sem disparar NullPointerException.
   */
  @Test
  public void deveRetornarNotasZeradasQuandoNaoHouverLancamentosExistentes() throws Exception {
    String aluno = "20261102";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, "PROF_A", periodo, 30, "24M12", "Sala_101"));

    DesempenhoFrequencia df =
        frequenciaService.calcularPercentualFrequencia(aluno, disciplina, periodo);

    assertEquals(0.0, df.getNotaEtapa1(), 0.01);
    assertEquals(0.0, df.getNotaEtapa2(), 0.01);

    double mediaCalculada = (df.getNotaEtapa1() + df.getNotaEtapa2()) / 2.0;
    assertEquals(0.0, mediaCalculada, 0.01);
  }

  /**
   * TESTE 3: Garante que o cálculo reflete os valores corretos quando apenas uma das notas foi
   * inserida pelo corpo docente (ex: apenas a nota da 1ª etapa).
   */
  @Test
  public void deveCalcularCorretamenteQuandoApenasUmaNotaEstiverLancada() throws Exception {
    String aluno = "20261103";
    String disciplina = "P1";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, "PROF_A", periodo, 30, "24M12", "Sala_101"));

    notaRepository.salvar(new Nota(aluno, disciplina, periodo, 9.0, 0.0, -1.0));

    DesempenhoFrequencia df =
        frequenciaService.calcularPercentualFrequencia(aluno, disciplina, periodo);

    assertEquals(9.0, df.getNotaEtapa1(), 0.01);
    assertEquals(0.0, df.getNotaEtapa2(), 0.01);

    double mediaCalculada = (df.getNotaEtapa1() + df.getNotaEtapa2()) / 2.0;
    assertEquals(4.5, mediaCalculada, 0.01);
  }
}
