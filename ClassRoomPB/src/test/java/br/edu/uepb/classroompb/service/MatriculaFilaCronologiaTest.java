package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MatriculaFilaCronologiaTest {

  private TurmaRepository turmaRepository;
  private MatriculaRepository matriculaRepository;
  private PeriodoRepository periodoRepository;
  private MatriculaService matriculaService;

  @Before
  public void setUp() throws Exception {
    // Garante a existência da pasta data para evitar quebras em builds limpos
    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }

    // Limpa os arquivos físicos para garantir isolamento total por execução de teste
    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();
    new File("data/periodos.txt").delete();

    this.turmaRepository = new TurmaRepository();
    this.matriculaRepository = new MatriculaRepository();
    this.periodoRepository = new PeriodoRepository();

    this.matriculaService =
        new MatriculaService(turmaRepository, matriculaRepository, periodoRepository);

    // Define o período como ativo e aberto para modificações
    periodoRepository.salvar(new Periodo("2027.1", "INICIADO"));
  }

  @Test
  public void deveManterOrdemCronologicaEstritaFilaEsperaFIFO() throws Exception {
    // 1. Configura uma turma restrita com limite de apenas 1 vaga física (US25)
    turmaRepository.salvar(new Turma("ES30", "PROF_X", "2027.1", 1, "08:00-10:00", "Sala 1"));

    // 2. Dispara a primeira matrícula: Aluno_A ocupa a única vaga física da turma
    matriculaService.solicitarMatricula("ALUNO_A", "ES30", "2027.1");

    // 3. Disparos sequenciais e consecutivos para a lista de espera
    matriculaService.solicitarMatricula(
        "ALUNO_B", "ES30", "2027.1"); // Deve ser o 1º da fila (Índice 1 no arquivo)
    matriculaService.solicitarMatricula(
        "ALUNO_C", "ES30", "2027.1"); // Deve ser o 2º da fila (Índice 2 no arquivo)
    matriculaService.solicitarMatricula(
        "ALUNO_D", "ES30", "2027.1"); // Deve ser o 3º da fila (Índice 3 no arquivo)

    // 4. Recupera os dados gravados fisicamente em disco para auditoria de mapeamento
    List<Matricula> salvas = matriculaRepository.buscarTodas();

    // Devem existir 4 matrículas no total registradas no arquivo plano
    assertEquals(
        "O arquivo plano deve conter exatamente 4 registros persistidos", 4, salvas.size());

    // Validação da vaga oficial do Aluno_A
    assertEquals("ALUNO_A", salvas.get(0).getMatriculaAluno());
    assertEquals(Matricula.StatusMatricula.SOLICITADA, salvas.get(0).getStatus());

    // VALIDAÇÃO CORE DA TASK 2281 (Assertivas Sequenciais de Correspondência Temporal / FIFO)
    assertEquals(
        "O primeiro elemento da fila (índice 1) deve ser o ALUNO_B",
        "ALUNO_B",
        salvas.get(1).getMatriculaAluno());
    assertEquals(
        "O status do ALUNO_B deve ser ESPERA",
        Matricula.StatusMatricula.ESPERA,
        salvas.get(1).getStatus());

    assertEquals(
        "O segundo elemento da fila (índice 2) deve ser o ALUNO_C",
        "ALUNO_C",
        salvas.get(2).getMatriculaAluno());
    assertEquals(
        "O status do ALUNO_C deve ser ESPERA",
        Matricula.StatusMatricula.ESPERA,
        salvas.get(2).getStatus());

    assertEquals(
        "O terceiro elemento da fila (índice 3) deve ser o ALUNO_D",
        "ALUNO_D",
        salvas.get(3).getMatriculaAluno());
    assertEquals(
        "O status do ALUNO_D deve ser ESPERA",
        Matricula.StatusMatricula.ESPERA,
        salvas.get(3).getStatus());
  }
}
