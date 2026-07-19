package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
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

  private static final String FILE_TURMAS = "data/turmas.txt";
  private static final String FILE_MATRICULAS = "data/matriculas.txt";
  private static final String FILE_FREQUENCIAS = "data/frequencias.txt";

  @Before
  public void setUp() throws Exception {
    // Regra de Estilo de Testes: Limpeza física preventiva dos arquivos plano
    File fTurmas = new File(FILE_TURMAS);
    if (fTurmas.exists()) fTurmas.delete();

    File fMatriculas = new File(FILE_MATRICULAS);
    if (fMatriculas.exists()) fMatriculas.delete();

    File fFrequencias = new File(FILE_FREQUENCIAS);
    if (fFrequencias.exists()) fFrequencias.delete();

    // Inicialização dos componentes reais
    turmaRepository = new TurmaRepository();
    matriculaRepository = new MatriculaRepository();
    frequenciaRepository = new FrequenciaRepository();
    notaRepository = new NotaRepository();

    frequenciaService =
        new FrequenciaService(
            turmaRepository, matriculaRepository, frequenciaRepository, notaRepository);
  }

  /** FLUXO PRINCIPAL: Valida o lançamento bem-sucedido de uma chamada pelo professor legítimo. */
  @Test
  public void deveRegistrarChamadaEmLoteComSucessoQuandoProfessorForDonoDaTurma() throws Exception {
    String profDono = "PROF_42";
    String disciplina = "ES01";
    String periodo = "2026.1";
    String dataAula = "27/06/2026";

    // Configura a infraestrutura de turma no arquivo plano
    turmaRepository.salvar(new Turma(disciplina, profDono, periodo, 40, "24M12", "Sala 1"));

    // Prepara uma lista simulada de alunos avaliados no diário de classe
    List<Matricula> alunosAvaliados = new ArrayList<>();

    Matricula m1 =
        new Matricula(
            "2026101",
            disciplina,
            periodo,
            Matricula.StatusMatricula.CONFIRMADA); // Irá como PRESENÇA
    Matricula m2 =
        new Matricula(
            "2026102", disciplina, periodo, Matricula.StatusMatricula.SOLICITADA); // Irá como FALTA

    alunosAvaliados.add(m1);
    alunosAvaliados.add(m2);

    // EXECUÇÃO: Tenta consolidar a chamada no diário
    frequenciaService.registrarChamadaLote(
        profDono, disciplina, periodo, dataAula, alunosAvaliados);

    // VERIFICAÇÃO: Checa se as duas linhas foram escritas com sucesso e com os Enums traduzidos
    List<Frequencia> gravadas = frequenciaRepository.buscarTodas();
    assertEquals(2, gravadas.size());

    assertEquals(Frequencia.TipoFrequencia.PRESENCA, gravadas.get(0).getStatus());
    assertEquals("2026101", gravadas.get(0).getMatriculaAluno());
  }

  /**
   * BARREIRA DE SEGURANÇA: Impede que um professor tente efetuar ou alterar chamadas em turmas de
   * terceiros.
   */
  @Test
  public void deveLancarExcecaoEAbortarQuandoProfessorNaoForResponsavelPelaTurma()
      throws Exception {
    String profVerdadeiro = "PROF_42";
    String profInvasor = "PROF_99";
    String disciplina = "ES01";
    String periodo = "2026.1";

    // Cadastra a turma vinculada ao professor verdadeiro
    turmaRepository.salvar(new Turma(disciplina, profVerdadeiro, periodo, 40, "24M12", "Sala 1"));

    List<Matricula> alunos = new ArrayList<>();
    alunos.add(new Matricula("2026101", disciplina, periodo, Matricula.StatusMatricula.CONFIRMADA));

    // EXECUÇÃO E ASSERÇÃO: O motor deve barrar e disparar ValidacaoException por quebra de
    // segurança
    assertThrows(
        ValidacaoException.class,
        () -> {
          frequenciaService.registrarChamadaLote(
              profInvasor, disciplina, periodo, "27/06/2026", alunos);
        });

    // O arquivo de frequências deve permanecer estritamente limpo/vazio
    assertTrue(frequenciaRepository.buscarTodas().isEmpty());
  }

  /** FLUXO ALTERNATIVO: Valida que o pipeline rejeita operações enviadas sem dados. */
  @Test
  public void deveLancarExcecaoSeAListaDeAlunosForEnviadaVazia() throws Exception {
    String prof = "PROF_42";
    String disciplina = "ES01";
    String periodo = "2026.1";

    turmaRepository.salvar(new Turma(disciplina, prof, periodo, 40, "24M12", "Sala 1"));

    assertThrows(
        ValidacaoException.class,
        () -> {
          frequenciaService.registrarChamadaLote(
              prof, disciplina, periodo, "27/06/2026", new ArrayList<>());
        });
  }
}
