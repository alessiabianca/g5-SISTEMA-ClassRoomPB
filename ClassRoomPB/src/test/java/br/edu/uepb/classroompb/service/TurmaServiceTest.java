package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Disciplina;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TurmaServiceTest {

    private TurmaService turmaService;
    private FakeTurmaRepository fakeTurmaRepository;
    private FakePeriodoRepository fakePeriodoRepository;
    private FakeDisciplinaRepository fakeDisciplinaRepository;

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

    // Criado para suprir a validação da Task 1904 de disciplina existente
    private static class FakeDisciplinaRepository extends DisciplinaRepository {
        private final List<Disciplina> disciplinasEmMemoria = new ArrayList<>();

        public void adicionarNoFake(Disciplina d) {
            disciplinasEmMemoria.add(d);
        }

        @Override
        public Disciplina buscarPorCodigo(String codigo) {
            for (Disciplina d : disciplinasEmMemoria) {
                if (d.getCodigo().equalsIgnoreCase(codigo)) {
                    return d;
                }
            }
            return null;
        }

        @Override
        public List<Disciplina> listarTodas() {
            return new ArrayList<>(disciplinasEmMemoria);
        }
    }

    @Before
    public void setUp() {
        fakeTurmaRepository = new FakeTurmaRepository();
        fakePeriodoRepository = new FakePeriodoRepository();
        fakeDisciplinaRepository = new FakeDisciplinaRepository();
        turmaService = new TurmaService(fakeTurmaRepository, fakePeriodoRepository, fakeDisciplinaRepository);
    }

    // ====================================================================
    // TESTES - OFERTA DE TURMA (TASK 1904 & TASK 1836 ATUALIZADOS)
    // ====================================================================

    @Test
    public void deveOfertarTurmaComSucessoQuandoPeriodoAtivoEDisciplinaExistente() throws Exception {
        // Para ter sucesso na US10 o período DEVE ser INICIADO e a disciplina deve existir
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
        fakeDisciplinaRepository.adicionarNoFake(new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

        turmaService.ofertarTurma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");
        assertEquals(1, fakeTurmaRepository.buscarTodas().size());
    }

    @Test
    public void deveLancarExcecaoQuandoDisciplinaNaoExistirNoSistema() {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
        // Não adicionamos a disciplina ES01 no repositório fake

        assertThrows(ValidacaoException.class, () -> {
            turmaService.ofertarTurma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");
        });
    }

    @Test
    public void deveLancarExcecaoQuandoPeriodoNaoEstiverAtivo() {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "PLANEJADO")); // Não está ativo
        fakeDisciplinaRepository.adicionarNoFake(new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

        assertThrows(ValidacaoException.class, () -> {
            turmaService.ofertarTurma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");
        });
    }

    @Test
    public void deveLancarExcecaoQuandoProfessorJaTemTurmaNoMesmoHorarioEPeriodo() throws Exception {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
        fakeDisciplinaRepository.adicionarNoFake(new Disciplina("ES01", "Engenharia de Software", 60, 4, null));
        fakeDisciplinaRepository.adicionarNoFake(new Disciplina("BD01", "Banco de Dados", 60, 4, null));
        
        fakeTurmaRepository.salvar(new Turma("BD01", "PROF_123", "2026.1", 30, "08:00-10:00", "Sala 2"));

        assertThrows(ChoqueHorarioException.class, () -> {
            turmaService.ofertarTurma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");
        });
    }

    @Test
    public void deveLancarExcecaoQuandoProfessorForNulo() {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
        fakeDisciplinaRepository.adicionarNoFake(new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

        assertThrows(IllegalArgumentException.class, () -> {
            turmaService.ofertarTurma("ES01", null, "2026.1", 40, "08:00-10:00", "Sala 1");
        });
    }

    @Test
    public void deveLancarExcecaoQuandoProfessorForVazio() {
        fakePeriodoRepository.adicionarNoFake(new Periodo("2026.1", "INICIADO"));
        fakeDisciplinaRepository.adicionarNoFake(new Disciplina("ES01", "Engenharia de Software", 60, 4, null));

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