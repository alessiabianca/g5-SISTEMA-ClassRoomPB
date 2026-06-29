package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MatriculaListaEsperaTest {

    private TurmaRepository turmaRepository;
    private MatriculaRepository matriculaRepository;
    private PeriodoRepository periodoRepository; // ADICIONADO: Dependência real do sistema
    private MatriculaService matriculaService;

    @Before
    public void setUp() throws Exception {
        // Garante a existência da pasta data para evitar FileNotFoundException
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Limpa os arquivos físicos antes de cada teste para garantir um ambiente isolado
        new File("data/turmas.txt").delete();
        new File("data/matriculas.txt").delete();
        new File("data/periodos.txt").delete();

        this.turmaRepository = new TurmaRepository();
        this.matriculaRepository = new MatriculaRepository();
        this.periodoRepository = new PeriodoRepository();
        
        // CORREÇÃO: Instanciando com os 3 repositórios obrigatórios do construtor real
        this.matriculaService = new MatriculaService(turmaRepository, matriculaRepository, periodoRepository);

        // Alimenta o repositório de períodos para a validação interna do motor passar
        periodoRepository.salvar(new Periodo("2026.2", "INICIADO"));
    }

    @Test
    public void deveAdicionarAlunoNaListaDeEsperaQuandoTurmaEstiverLotada() throws Exception {
        // 1. Prepara o cenário: Cria uma turma com apenas 1 VAGA disponível
        Turma turma = new Turma("ES01", "PROF_123", "2026.2", 1, "08:00-10:00", "Sala 1");
        turmaRepository.salvar(turma);

        // 2. Aluno 1 ocupa a única vaga disponível
        // CORREÇÃO: Removido o parâmetro de lista do método para adequar à assinatura real
        Matricula mat1 = matriculaService.solicitarMatricula("ALUNO_001", "ES01", "2026.2");
        
        // Valida se o Aluno 1 conseguiu a vaga
        assertNotNull(mat1);
        assertEquals(Matricula.StatusMatricula.SOLICITADA, mat1.getStatus());

        // 3. Aluno 2 tenta se matricular na mesma turma (que agora já está lotada)
        // CORREÇÃO: Removido o parâmetro extra. O service já lê o arquivo físico atualizado por baixo dos panos!
        Matricula mat2 = matriculaService.solicitarMatricula("ALUNO_002", "ES01", "2026.2");

        // Valida se a regra de negócio colocou o Aluno 2 na espera
        assertNotNull(mat2);
        assertEquals(Matricula.StatusMatricula.ESPERA, mat2.getStatus());

        // 4. Confirmação final da integridade dos dados no repositório
        List<Matricula> todasAsMatriculasSalvas = matriculaRepository.buscarTodas();
        assertEquals(2, todasAsMatriculasSalvas.size());
    }
}