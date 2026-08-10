package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class TurmaFilaConsultaTest {

  private TurmaRepository turmaRepository;
  private MatriculaRepository matriculaRepository;
  private TurmaService turmaService;

  @Before
  public void setUp() throws Exception {

    File dataDir = new File("data");
    if (!dataDir.exists()) {
      dataDir.mkdirs();
    }

    new File("data/turmas.txt").delete();
    new File("data/matriculas.txt").delete();

    this.turmaRepository = new TurmaRepository();
    this.matriculaRepository = new MatriculaRepository();

    this.turmaService =
        new TurmaService(turmaRepository, new PeriodoRepository(), new DisciplinaRepository());
  }

  @Test
  public void deveRetornarListaVaziaSemNullPointerExceptionSeNaoHouverFila() throws Exception {

    turmaRepository.salvar(new Turma("ES35", "2027.1", 30));

    List<Matricula> fila = turmaService.obterListaEspera("ES35", "2027.1");

    assertNotNull("A lista retornada nunca deve ser nula para evitar NullPointerException", fila);
    assertEquals(
        "O tamanho da lista de espera deve ser zero para turmas sem fila ativa", 0, fila.size());
  }

  @Test
  public void deveRetornarContagemExataDeAlunosNaListaDeEspera() throws Exception {

    turmaRepository.salvar(new Turma("ES35", "2027.1", 1));

    matriculaRepository.salvar(
        new Matricula("0001", "ES35", "2027.1", Matricula.StatusMatricula.CONFIRMADA));
    matriculaRepository.salvar(
        new Matricula("0002", "ES35", "2027.1", Matricula.StatusMatricula.ESPERA));
    matriculaRepository.salvar(
        new Matricula("0003", "ES35", "2027.1", Matricula.StatusMatricula.ESPERA));

    List<Matricula> fila = turmaService.obterListaEspera("ES35", "2027.1");

    assertNotNull(fila);
    assertEquals(
        "A contagem exposta deve bater exatamente com os dados reais (2 alunos na fila)",
        2,
        fila.size());
    assertEquals(
        "O primeiro aluno da lista retornada deve ser a matrícula 0002",
        "0002",
        fila.get(0).getMatriculaAluno());
    assertEquals(
        "O segundo aluno da lista retornada deve ser a matrícula 0003",
        "0003",
        fila.get(1).getMatriculaAluno());
  }
}
