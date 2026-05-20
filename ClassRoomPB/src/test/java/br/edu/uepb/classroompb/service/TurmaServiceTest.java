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

    /**
     * Implementação em memória do TurmaRepository.
     */
    private static class FakeTurmaRepository extends TurmaRepository {
        private final List<Turma> turmasEmMemoria = new ArrayList<>();

        @Override
        public void salvar(Turma turma) {
            turmasEmMemoria.add(turma);
        }

        @Override
        public List<Turma> buscarTodas() {
            return turmasEmMemoria;
        }

        @Override
        public void atualizarArquivoCompleto(List<Turma> turmasAtualizadas) {
            turmasEmMemoria.clear();
            turmasEmMemoria.addAll(turmasAtualizadas);
        }
    }

    /**
     * Implementação em memória do PeriodoRepository.
     */
    private static class FakePeriodoRepository extends PeriodoRepository {
        private final List<Periodo> periodosEmMemoria = new ArrayList<>();
        
        public void salvar(Periodo p) {
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
    }

    @Before
    public void setUp() {
        fakeTurmaRepository = new FakeTurmaRepository();
        fakePeriodoRepository = new FakePeriodoRepository();
        turmaService = new TurmaService(fakeTurmaRepository, fakePeriodoRepository);
    }

    // ====================================================================
    // TESTES - OFERTA DE TURMA E CHOQUE DE HORÁRIO
    // ====================================================================

    @Test
    public void deveOfertarTurmaComSucessoQuandoProfessorLivre() {
        turmaService.ofertarTurma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");

        List<Turma> turmasSalvas = fakeTurmaRepository.buscarTodas();
        assertEquals(1, turmasSalvas.size());
        assertEquals("PROF_123", turmasSalvas.get(0).getMatriculaProfessor());
    }

    @Test
    public void deveLancarExcecaoQuandoProfessorJaTemTurmaNoMesmoHorarioEPeriodo() {
        Turma turmaExistente = new Turma("BD01", "PROF_123", "2026.1", 30, "08:00-10:00", "Sala 2");
        fakeTurmaRepository.salvar(turmaExistente);

        ChoqueHorarioException excecao = assertThrows(ChoqueHorarioException.class, () -> {
            turmaService.ofertarTurma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");
        });

        assertTrue(excecao.getMessage().contains("Choque de horário"));
        assertEquals(1, fakeTurmaRepository.buscarTodas().size());
    }

    @Test
    public void devePermitirProfessorLecionarNoMesmoHorarioEmPeriodosDiferentes() {
        Turma turmaPeriodoAnterior = new Turma("BD01", "PROF_123", "2026.1", 30, "08:00-10:00", "Sala 2");
        fakeTurmaRepository.salvar(turmaPeriodoAnterior);

        turmaService.ofertarTurma("ES01", "PROF_123", "2026.2", 40, "08:00-10:00", "Sala 1");

        assertEquals(2, fakeTurmaRepository.buscarTodas().size());
    }

    // ====================================================================
    // TESTES - EDIÇÃO E CANCELAMENTO COM VALIDAÇÃO DE PERÍODO
    // ====================================================================

    @Test
    public void deveImpedirEdicaoSePeriodoEstiverIniciado() {
        fakePeriodoRepository.salvar(new Periodo("2026.1", "INICIADO"));
        fakeTurmaRepository.salvar(new Turma("ES01", "PROF_123", "2026.1", 30, "08:00-10:00", "Sala 1"));

        IllegalStateException excecao = assertThrows(IllegalStateException.class, () -> {
            turmaService.editarTurma("ES01", "2026.1", 40, "10:00-12:00", "Sala 2");
        });

        assertTrue(excecao.getMessage().contains("Ação bloqueada"));
    }

    @Test
    public void deveImpedirCancelamentoSePeriodoEstiverEncerrado() {
        fakePeriodoRepository.salvar(new Periodo("2025.2", "ENCERRADO"));
        fakeTurmaRepository.salvar(new Turma("BD01", "PROF_456", "2025.2", 40, "14:00-16:00", "Lab 1"));

        IllegalStateException excecao = assertThrows(IllegalStateException.class, () -> {
            turmaService.cancelarTurma("BD01", "2025.2");
        });

        assertTrue(excecao.getMessage().contains("Ação bloqueada"));
        assertEquals(1, fakeTurmaRepository.buscarTodas().size()); // Garante que a turma não foi apagada
    }
}