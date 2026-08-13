package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Aula;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.AulaRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FrequenciaServiceTest {

  private FrequenciaService frequenciaService;
  private TurmaRepository turmaRepository;
  private MatriculaRepository matriculaRepository;
  private FrequenciaRepository frequenciaRepository;
  private NotaRepository notaRepository;
  private DiarioRepository diarioRepository;
  private AulaRepository aulaRepository;

  private static final String FILE_TURMAS = "data/turmas.txt";
  private static final String FILE_MATRICULAS = "data/matriculas.txt";
  private static final String FILE_FREQUENCIAS = "data/frequencias.txt";
  private static final String FILE_DIARIOS = "data/diarios.txt";
  private static final String FILE_AULAS = "data/aulas.txt";

  @Before
  public void setUp() throws Exception {

    File fTurmas = new File(FILE_TURMAS);
    if (fTurmas.exists()) fTurmas.delete();

    File fMatriculas = new File(FILE_MATRICULAS);
    if (fMatriculas.exists()) fMatriculas.delete();

    File fFrequencias = new File(FILE_FREQUENCIAS);
    if (fFrequencias.exists()) fFrequencias.delete();

    File fDiarios = new File(FILE_DIARIOS);
    if (fDiarios.exists()) fDiarios.delete();

    File fAulas = new File(FILE_AULAS);
    if (fAulas.exists()) fAulas.delete();

    turmaRepository = new TurmaRepository();
    matriculaRepository = new MatriculaRepository();
    frequenciaRepository = new FrequenciaRepository();
    notaRepository = new NotaRepository();
    diarioRepository = new DiarioRepository();
    aulaRepository = new AulaRepository();

    frequenciaService =
        new FrequenciaService(
            turmaRepository,
            matriculaRepository,
            frequenciaRepository,
            notaRepository,
            diarioRepository,
            aulaRepository);
  }

  @Test
  public void deveRegistrarChamadaEmLoteComSucessoQuandoProfessorForDonoDoDiario()
      throws Exception {
    String profDono = "PROF_42";
    String disciplina = "ES01";
    String periodo = "2026.1";
    String codDiario = "DIARIO_01";
    String idAula = "AULA_01";

    turmaRepository.salvar(new Turma(disciplina, periodo, 40));
    matriculaRepository.salvar(
        new Matricula("2026101", disciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("2026102", disciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));
    diarioRepository.salvar(
        new Diario(
            codDiario, disciplina, periodo, "Diario Teste", profDono, "08:00-10:00", "Sala 1", 60));
    aulaRepository.salvar(new Aula(idAula, codDiario, "27/06/2026", "Assunto", 2));

    List<Matricula> alunosAvaliados = new ArrayList<>();
    alunosAvaliados.add(
        new Matricula("2026101", disciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));
    alunosAvaliados.add(
        new Matricula("2026102", disciplina, periodo, Matricula.StatusMatricula.SOLICITADA));

    frequenciaService.registrarChamadaLote(profDono, codDiario, idAula, alunosAvaliados);

    List<Frequencia> gravadas = frequenciaRepository.buscarTodas();
    assertEquals(2, gravadas.size());
    assertEquals(Frequencia.TipoFrequencia.PRESENCA, gravadas.get(0).getStatus());
    assertEquals("2026101", gravadas.get(0).getMatriculaAluno());
    assertEquals(codDiario, gravadas.get(0).getCodigoDiario());
    assertEquals(idAula, gravadas.get(0).getIdAula());
  }

  @Test
  public void deveBloquearChamadaParaAlunoSemMatriculaConfirmadaNaTurma() throws Exception {
    String profDono = "PROF_42";
    String disciplina = "ES01";
    String periodo = "2026.1";
    String codDiario = "DIARIO_01";
    String idAula = "AULA_01";

    turmaRepository.salvar(new Turma(disciplina, periodo, 40));
    diarioRepository.salvar(
        new Diario(
            codDiario, disciplina, periodo, "Diario Teste", profDono, "08:00-10:00", "Sala 1", 60));
    aulaRepository.salvar(new Aula(idAula, codDiario, "27/06/2026", "Assunto", 2));

    List<Matricula> alunos = new ArrayList<>();
    alunos.add(new Matricula("2026999", disciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));

    assertThrows(
        ValidacaoException.class,
        () -> {
          frequenciaService.registrarChamadaLote(profDono, codDiario, idAula, alunos);
        });

    assertTrue(frequenciaRepository.buscarTodas().isEmpty());
  }

  @Test
  public void deveLancarExcecaoEAbortarQuandoProfessorNaoForResponsavelPeloDiario()
      throws Exception {
    String profInvasor = "PROF_99";
    String profDono = "PROF_42";
    String disciplina = "ES01";
    String periodo = "2026.1";
    String codDiario = "DIARIO_01";
    String idAula = "AULA_01";

    turmaRepository.salvar(new Turma(disciplina, periodo, 40));
    diarioRepository.salvar(
        new Diario(
            codDiario, disciplina, periodo, "Diario Teste", profDono, "08:00-10:00", "Sala 1", 60));
    aulaRepository.salvar(new Aula(idAula, codDiario, "27/06/2026", "Assunto", 2));

    List<Matricula> alunos = new ArrayList<>();
    alunos.add(new Matricula("2026101", disciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));

    assertThrows(
        ValidacaoException.class,
        () -> {
          frequenciaService.registrarChamadaLote(profInvasor, codDiario, idAula, alunos);
        });

    assertTrue(frequenciaRepository.buscarTodas().isEmpty());
  }

  @Test
  public void deveBloquearLancamentoQuandoDiarioEstiverFechado() throws Exception {
    String profDono = "PROF_42";
    String disciplina = "ES01";
    String periodo = "2026.1";
    String codDiario = "DIARIO_01";
    String idAula = "AULA_01";

    turmaRepository.salvar(new Turma(disciplina, periodo, 40));
    Diario diario =
        new Diario(
            codDiario,
            disciplina,
            periodo,
            "Diario Teste",
            profDono,
            "08:00-10:00",
            "Sala 1",
            60,
            Diario.SituacaoDiario.FECHADO);
    diarioRepository.salvar(diario);
    aulaRepository.salvar(new Aula(idAula, codDiario, "27/06/2026", "Assunto", 2));

    List<Matricula> alunos = new ArrayList<>();
    alunos.add(new Matricula("2026101", disciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));

    assertThrows(
        ValidacaoException.class,
        () -> {
          frequenciaService.registrarChamadaLote(profDono, codDiario, idAula, alunos);
        });

    assertTrue(frequenciaRepository.buscarTodas().isEmpty());
  }

  @Test
  public void deveLancarExcecaoSeAListaDeAlunosForEnviadaVazia() throws Exception {
    String profDono = "PROF_42";
    String disciplina = "ES01";
    String periodo = "2026.1";
    String codDiario = "DIARIO_01";
    String idAula = "AULA_01";

    turmaRepository.salvar(new Turma(disciplina, periodo, 40));
    diarioRepository.salvar(
        new Diario(
            codDiario, disciplina, periodo, "Diario Teste", profDono, "08:00-10:00", "Sala 1", 60));
    aulaRepository.salvar(new Aula(idAula, codDiario, "27/06/2026", "Assunto", 2));

    assertThrows(
        ValidacaoException.class,
        () -> {
          frequenciaService.registrarChamadaLote(profDono, codDiario, idAula, new ArrayList<>());
        });
  }
}
