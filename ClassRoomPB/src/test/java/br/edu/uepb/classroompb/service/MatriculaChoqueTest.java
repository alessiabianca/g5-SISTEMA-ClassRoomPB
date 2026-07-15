package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioAlunoException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class MatriculaChoqueTest {

  private TurmaService turmaService;
  private TurmaRepository turmaRepository;
  private PeriodoRepository periodoRepository;
  private DisciplinaRepository disciplinaRepository;
  private MatriculaRepository matriculaRepository;

  private static final String FILE_TURMAS = "data/turmas.txt";
  private static final String FILE_MATRICULAS = "data/matriculas.txt";

  @Before
  public void setUp() throws Exception {
    // Limpa os arquivos físicos de teste para garantir isolamento e ambiente limpo
    File fTurmas = new File(FILE_TURMAS);
    if (fTurmas.exists()) fTurmas.delete();

    File fMatriculas = new File(FILE_MATRICULAS);
    if (fMatriculas.exists()) fMatriculas.delete();

    // Inicializa os repositórios reais mapeando o diretório de testes local
    turmaRepository = new TurmaRepository();
    periodoRepository = new PeriodoRepository();
    disciplinaRepository = new DisciplinaRepository();
    matriculaRepository = new MatriculaRepository();

    // Cria o serviço centralizador
    turmaService = new TurmaService(turmaRepository, periodoRepository, disciplinaRepository);
  }

  @Test
  public void devePermitirMatriculaQuandoNaoHouverNenhumChoqueDeHorario() throws Exception {
    String aluno = "20261001";
    String periodo = "2026.1";

    // Configura duas turmas em horários completamente diferentes (Disjuntos)
    turmaRepository.salvar(new Turma("ES01", "PROF_A", periodo, 40, "24M12", "Sala 1"));
    turmaRepository.salvar(new Turma("BD01", "123", periodo, 40, "35M34", "Sala 2"));

    // CORREÇÃO: Utiliza o Enum StatusMatricula.CONFIRMADA
    matriculaRepository.salvar(
        new Matricula(aluno, "ES01", periodo, Matricula.StatusMatricula.CONFIRMADA));

    // Execução do motor antichoques tentando colocar o aluno na segunda disciplina (BD01)
    turmaService.validarChoqueHorarioAluno(aluno, "BD01", periodo);
  }

  @Test
  public void deveLancarExcecaoQuandoHouverChoqueTotalDeHorarioDoAluno() throws Exception {
    String aluno = "20261002";
    String periodo = "2026.1";

    // CORREÇÃO: Configura as duas turmas com o MESMO horário exato para forçar a colisão
    turmaRepository.salvar(new Turma("ES01", "PROF_A", periodo, 40, "24M12", "Sala_101"));
    turmaRepository.salvar(new Turma("BD01", "PROF_B", periodo, 40, "24M12", "Sala_102"));

    // CORREÇÃO: Utiliza o Enum StatusMatricula.SOLICITADA
    matriculaRepository.salvar(
        new Matricula(aluno, "ES01", periodo, Matricula.StatusMatricula.SOLICITADA));

    // O motor deve interceptar e abortar a operação lançando ChoqueHorarioAlunoException
    assertThrows(
        ChoqueHorarioAlunoException.class,
        () -> {
          turmaService.validarChoqueHorarioAluno(aluno, "BD01", periodo);
        });
  }

  @Test
  public void devePermitirMesmoHorarioSeOsPeriodosLetivosForemDiferentes() throws Exception {
    String aluno = "20261003";

    // CORREÇÃO: Removido o parâmetro '0' (vagas ocupadas) das turmas para alinhar com o leitor de 6
    // colunas do arquivo
    turmaRepository.salvar(new Turma("ES01", "PROF_A", "2026.1", 40, "24M12", "Sala 1"));
    turmaRepository.salvar(new Turma("BD01", "PROF_B", "2026.2", 40, "24M12", "Sala 2"));

    // CORREÇÃO: Utiliza o Enum StatusMatricula.CONFIRMADA
    matriculaRepository.salvar(
        new Matricula(aluno, "ES01", "2026.1", Matricula.StatusMatricula.CONFIRMADA));

    // Deve liberar com sucesso, pois não há colisão dentro do mesmo período letivo acadêmico
    turmaService.validarChoqueHorarioAluno(aluno, "BD01", "2026.2");
  }

  @Test
  public void deveLancarExcecaoSeATurmaDesejadaNaoExistirNoCatalogo() {
    String aluno = "20261004";
    String periodo = "2026.1";

    // Força uma falha de validação comum caso o código da oferta seja fantasma
    assertThrows(
        ValidacaoException.class,
        () -> {
          turmaService.validarChoqueHorarioAluno(aluno, "DIREITO_01", periodo);
        });
  }
}
