package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class RelatorioAlunosMatriculadosTest {

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

        turmaRepository = new TurmaRepository();
        matriculaRepository = new MatriculaRepository();
        turmaService = new TurmaService(turmaRepository, new PeriodoRepository(), new DisciplinaRepository());
    }

    @Test
    public void deveGerarRelatorioComApenasAlunosConfirmadosDaTurma() throws Exception {
        turmaRepository.salvar(new Turma("ES40", "PROF_RF40", "2026.2", 30, "24M12", "Sala_RF40"));
        turmaRepository.salvar(new Turma("BD40", "PROF_RF40", "2026.2", 30, "35M12", "Sala_BD"));
        turmaRepository.salvar(new Turma("ES40", "PROF_RF40", "2027.1", 30, "46M12", "Sala_2027"));

        matriculaRepository.salvar(new Matricula("ALUNO_001", "ES40", "2026.2", Matricula.StatusMatricula.CONFIRMADA));
        matriculaRepository.salvar(new Matricula("ALUNO_002", "ES40", "2026.2", Matricula.StatusMatricula.CONFIRMADA));
        matriculaRepository.salvar(new Matricula("ALUNO_ESPERA", "ES40", "2026.2", Matricula.StatusMatricula.ESPERA));
        matriculaRepository.salvar(new Matricula("ALUNO_REJEITADO", "ES40", "2026.2", Matricula.StatusMatricula.REJEITADA));
        matriculaRepository.salvar(new Matricula("ALUNO_OUTRA_TURMA", "BD40", "2026.2", Matricula.StatusMatricula.CONFIRMADA));
        matriculaRepository.salvar(new Matricula("ALUNO_OUTRO_PERIODO", "ES40", "2027.1", Matricula.StatusMatricula.CONFIRMADA));

        List<Matricula> relatorio = turmaService.gerarRelatorioAlunosMatriculados("ES40", "2026.2");

        assertNotNull(relatorio);
        assertEquals("O relatório deve conter apenas as matrículas confirmadas da turma solicitada", 2, relatorio.size());
        assertEquals("ALUNO_001", relatorio.get(0).getMatriculaAluno());
        assertEquals("ALUNO_002", relatorio.get(1).getMatriculaAluno());
    }

    @Test
    public void deveRetornarListaVaziaQuandoTurmaNaoPossuirAlunosMatriculados() throws Exception {
        turmaRepository.salvar(new Turma("ES40", "PROF_RF40", "2026.2", 30, "24M12", "Sala_RF40"));
        matriculaRepository.salvar(new Matricula("ALUNO_ESPERA", "ES40", "2026.2", Matricula.StatusMatricula.ESPERA));

        List<Matricula> relatorio = turmaService.gerarRelatorioAlunosMatriculados("ES40", "2026.2");

        assertNotNull("O relatório deve retornar uma lista vazia, nunca nula", relatorio);
        assertTrue("Turmas sem alunos confirmados devem gerar relatório vazio", relatorio.isEmpty());
    }

    @Test
    public void deveLancarExcecaoQuandoTurmaDoRelatorioNaoExistir() throws Exception {
        try {
            turmaService.gerarRelatorioAlunosMatriculados("TURMA_INEXISTENTE", "2026.2");
            fail("Deveria ter lançado ValidacaoException para turma inexistente.");
        } catch (ValidacaoException e) {
            assertTrue(e.getMessage().contains("turma informada não existe"));
        }
    }
}
