package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TurmaServiceTest {

    private TurmaService turmaService;
    private FakeTurmaRepository fakeRepository;

    /**
     * Implementação em memória do TurmaRepository.
     * Isola os testes de unidade da camada de serviço, garantindo que o 
     * ficheiro real (turmas.txt) não seja modificado durante a execução.
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

    @Before
    public void setUp() {
        fakeRepository = new FakeTurmaRepository();
        turmaService = new TurmaService(fakeRepository);
    }

    @Test
    public void deveOfertarTurmaComSucessoQuandoProfessorLivre() {
        turmaService.ofertarTurma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");

        List<Turma> turmasSalvas = fakeRepository.buscarTodas();
        assertEquals(1, turmasSalvas.size());
        assertEquals("PROF_123", turmasSalvas.get(0).getMatriculaProfessor());
    }

    @Test
    public void deveLancarExcecaoQuandoProfessorJaTemTurmaNoMesmoHorarioEPeriodo() {
        Turma turmaExistente = new Turma("BD01", "PROF_123", "2026.1", 30, "08:00-10:00", "Sala 2");
        fakeRepository.salvar(turmaExistente);

        ChoqueHorarioException excecao = assertThrows(ChoqueHorarioException.class, () -> {
            turmaService.ofertarTurma("ES01", "PROF_123", "2026.1", 40, "08:00-10:00", "Sala 1");
        });

        assertTrue(excecao.getMessage().contains("Choque de horário"));
        assertEquals(1, fakeRepository.buscarTodas().size());
    }

    @Test
    public void devePermitirProfessorLecionarNoMesmoHorarioEmPeriodosDiferentes() {
        Turma turmaPeriodoAnterior = new Turma("BD01", "PROF_123", "2026.1", 30, "08:00-10:00", "Sala 2");
        fakeRepository.salvar(turmaPeriodoAnterior);

        turmaService.ofertarTurma("ES01", "PROF_123", "2026.2", 40, "08:00-10:00", "Sala 1");

        assertEquals(2, fakeRepository.buscarTodas().size());
    }
}