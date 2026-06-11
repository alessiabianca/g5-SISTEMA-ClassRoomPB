package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MatriculaListaEsperaTest {

    private TurmaRepository turmaRepository;
    private MatriculaRepository matriculaRepository;
    private MatriculaService matriculaService;

    @Before
    public void setUp() {
        // Limpa os arquivos físicos antes de cada teste para garantir um ambiente isolado
        new File("data/turmas.txt").delete();
        new File("data/matriculas.txt").delete();

        this.turmaRepository = new TurmaRepository();
        this.matriculaRepository = new MatriculaRepository();
        this.matriculaService = new MatriculaService(turmaRepository, matriculaRepository);
    }

    @Test
    public void deveAdicionarAlunoNaListaDeEsperaQuandoTurmaEstiverLotada() throws Exception {
        // 1. Prepara o cenário: Cria uma turma com apenas 1 VAGA disponível
        Turma turma = new Turma("ES01", "PROF_123", "2026.2", 1, "08:00-10:00", "Sala 1");
        turmaRepository.salvar(turma);

        // 2. Aluno 1 ocupa a única vaga disponível
        List<Matricula> estadoVazio = matriculaRepository.buscarTodas();
        Matricula mat1 = matriculaService.solicitarMatricula("ALUNO_001", "ES01", "2026.2", estadoVazio);
        matriculaRepository.salvar(mat1); // Persiste a ocupação da vaga

        // Valida se o Aluno 1 conseguiu a vaga
        assertNotNull(mat1);
        assertEquals(Matricula.StatusMatricula.SOLICITADA, mat1.getStatus());

        // 3. Aluno 2 tenta se matricular na mesma turma (que agora já está lotada)
        List<Matricula> estadoComUmaOcupada = matriculaRepository.buscarTodas();
        Matricula mat2 = matriculaService.solicitarMatricula("ALUNO_002", "ES01", "2026.2", estadoComUmaOcupada);
        matriculaRepository.salvar(mat2); // Persiste a entrada na lista de espera

        // Valida se a regra de negócio colocou o Aluno 2 na espera
        assertNotNull(mat2);
        assertEquals(Matricula.StatusMatricula.ESPERA, mat2.getStatus());

        // 4. Confirmação final da integridade dos dados no repositório
        List<Matricula> todasAsMatriculasSalvas = matriculaRepository.buscarTodas();
        assertEquals(2, todasAsMatriculasSalvas.size());
    }
}