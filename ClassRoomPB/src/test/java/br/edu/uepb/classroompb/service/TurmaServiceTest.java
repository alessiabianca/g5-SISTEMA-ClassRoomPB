// src/test/java/br/edu/uepb/classroompb/service/TurmaServiceTest.java
package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TurmaServiceTest {

    private TurmaService turmaService;
    private FakeTurmaRepository fakeTurmaRepository;
    private FakePeriodoRepository fakePeriodoRepository;

    private static class FakeTurmaRepository extends TurmaRepository {
        private final List<Turma> turmasEmMemoria = new ArrayList<>();

        @Override
        public void salvar(Turma turma) {
            turmasEmMemoria.add(turma);
        }

        @Override
        public List<Turma> buscarTodas() {
            return new ArrayList<>(turmasEmMemoria);
        }

        @Override
        public void atualizarArquivoCompleto(List<Turma> turmasAtualizadas) {
            turmasEmMemoria.clear();
            turmasEmMemoria.addAll(turmasAtualizadas);
        }
    }

    private static class FakePeriodoRepository extends PeriodoRepository {
        private final List<Periodo> periodosEmMemoria = new ArrayList<>();
        
        public void adicionarNoFake(Periodo p) {
            periodosEmMemoria.add(p);
        }

        @Override
        public Periodo buscarPorCodigo(String codigo) {
            for (Periodo p : periodosEmMemoria) {
                if (p.getCodigo().equalsIgnoreCase(codigo)) {
                    return p;
                }
            }
            return null;
        }

        public List<Periodo> listarTodos() {
            return new ArrayList<>(periodosEmMemoria);

        }
    }

    @Before
    public void setUp() {
        fakeTurmaRepository = new FakeTurmaRepository();
        fakePeriodoRepository = new FakePeriodoRepository();
        turmaService = new TurmaService(fakeTurmaRepository, fakePeriodoRepository);
    }

    // ====================================================================
    // TESTES - OFERTA DE TURMA (TASK 1836 INCLUÍDA)
    // ====================================================================

    @Test
    public void deveOfertarTurmaComSucessoQuandoProfessorLivre() throws Exception {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "PLANEJADO"));
        turmaService.ofertarTurma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");
        assertEquals(1, fakeTurmaRepository.buscarTodas().size());
    }

    @Test
    public void deveLancarExcecaoQuandoProfessorJaTemTurmaNoMesmoHorarioEPeriodo() throws Exception {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "PLANEJADO"));
        fakeTurmaRepository.salvar(new Turma("BD01", "PROF_123", "2026.1", 30, "08:00-10:00", "Sala 2"));

        assertThrows(ChoqueHorarioException.class, () -> {
            turmaService.ofertarTurma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");
        });
    }

    @Test
    public void deveLancarExcecaoQuandoProfessorForNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            turmaService.ofertarTurma("ES01", null, "2026.1", 40, "08:00-10:00", "Sala 1");
        });
    }

    @Test
    public void deveLancarExcecaoQuandoProfessorForVazio() {
        assertThrows(IllegalArgumentException.class, () -> {
            turmaService.ofertarTurma("ES01", "   ", "2026.1", 40, "08:00-10:00", "Sala 1");
        });
    }

    // ====================================================================
    // TESTES - EDIÇÃO E CANCELAMENTO (CENÁRIOS DE SUCESSO)
    // ====================================================================

    @Test
    public void deveEditarTurmaComSucessoQuandoPeriodoEstiverPlanejado() throws Exception {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.2", "PLANEJADO"));
        fakeTurmaRepository.salvar(new Turma("ES01", "PROF_123", "2026.2", 30, "08:00-10:00", "Sala 1"));

        turmaService.editarTurma("ES01", "2026.2", 50, "14:00-16:00", "Lab 3");

        List<Turma> turmas = fakeTurmaRepository.buscarTodas();
        assertEquals(1, turmas.size());
        
        Turma turmaEditada = turmas.get(0);
        assertEquals(50, turmaEditada.getVagas());
        assertEquals("14:00-16:00", turmaEditada.getHorario());
        assertEquals("Lab 3", turmaEditada.getSala());
    }

    @Test
    public void deveCancelarTurmaComSucessoRemovendoDoRepositorio() throws Exception {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.2", "PLANEJADO"));
        fakeTurmaRepository.salvar(new Turma("ES01", "PROF_123", "2026.2", 30, "08:00-10:00", "Sala 1"));
        fakeTurmaRepository.salvar(new Turma("BD01", "PROF_456", "2026.2", 40, "10:00-12:00", "Sala 2"));

        assertEquals(2, fakeTurmaRepository.buscarTodas().size());

        turmaService.cancelarTurma("ES01", "2026.2");

        List<Turma> turmasRestantes = fakeTurmaRepository.buscarTodas();
        assertEquals(1, turmasRestantes.size());
        assertEquals("BD01", turmasRestantes.get(0).getCodigoDisciplina());
    }

    // ====================================================================
    // TESTES - EDIÇÃO E CANCELAMENTO (CENÁRIOS DE BLOQUEIO)
    // ====================================================================

    @Test
    public void deveImpedirEdicaoSePeriodoEstiverIniciado() throws Exception {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
        fakeTurmaRepository.salvar(new Turma("ES01", "PROF_123", "2026.1", 30, "08:00-10:00", "Sala 1"));

        assertThrows(IllegalStateException.class, () -> {
            turmaService.editarTurma("ES01", "2026.1", 40, "10:00-12:00", "Sala 2");
        });
    }

    @Test
    public void deveImpedirCancelamentoSePeriodoEstiverEncerrado() throws Exception {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2025.2", "ENCERRADO"));
        fakeTurmaRepository.salvar(new Turma("BD01", "PROF_456", "2025.2", 40, "14:00-16:00", "Lab 1"));

        assertThrows(IllegalStateException.class, () -> {
            turmaService.cancelarTurma("BD01", "2025.2");
        });
    }
}