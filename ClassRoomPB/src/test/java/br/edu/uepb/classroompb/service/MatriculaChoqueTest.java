package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioAlunoException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import static org.junit.Assert.*;

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
        // Simula que o aluno já está confirmado na primeira disciplina (ES01)
        matriculaRepository.salvar(new Matricula(aluno, "ES01", periodo, "CONFIRMADA"));

        // Execução do motor antichoques tentando colocar o aluno na segunda disciplina (BD01)
        // Não deve lançar nenhuma exceção, pois 24M12 e 35M34 são horários válidos e distintos
        turmaService.validarChoqueHorarioAluno(aluno, "BD01", periodo);
    }

    @Test
    public void deveLancarExcecaoQuandoHouverChoqueTotalDeHorarioDoAluno() throws Exception {
        String aluno = "20261002";
        String periodo = "2026.1";

        // Configura duas turmas diferentes ocorrendo exatamente no mesmo dia e horário (Choque Total)
         
        turmaRepository.salvar(new Turma("ES01", "PROF_A", periodo, 40, "24M12", "Sala_101"));
        turmaRepository.salvar(new Turma("BD01", "PROF_B", periodo, 40, "35M12", "Sala_102"));

        // Caso o aluno já possua vínculo (SOLICITADA ou CONFIRMADA) na primeira turma
        matriculaRepository.salvar(new Matricula(aluno, "ES01", periodo, "SOLICITADA"));

        // O motor deve interceptar e abortar a operação para a segunda turma de mesmo horário
        assertThrows(ChoqueHorarioAlunoException.class, () -> {
            turmaService.validarChoqueHorarioAluno(aluno, "BD01", periodo);
        });
    }

    @Test
    public void devePermitirMesmoHorarioSeOsPeriodosLetivosForemDiferentes() throws Exception {
        String aluno = "20261003";
        
        // Configura a mesma disciplina/horário mas dividida em semestres cronologicamente distantes
        turmaRepository.salvar(new Turma("ES01", "PROF_A", "2026.1", 40, 0, "24M12", "Sala 1"));
        turmaRepository.salvar(new Turma("BD01", "PROF_B", "2026.2", 40, 0, "24M12", "Sala 2"));

        // Aluno matriculado no primeiro semestre
        matriculaRepository.salvar(new Matricula(aluno, "ES01", "2026.1", "CONFIRMADA"));

        // Deve liberar com sucesso, pois não há colisão dentro do mesmo período letivo acadêmico
        turmaService.validarChoqueHorarioAluno(aluno, "BD01", "2026.2");
    }

    @Test
    public void deveLancarExcecaoSeATurmaDesejadaNaoExistirNoCatalogo() {
        String aluno = "20261004";
        String periodo = "2026.1";

        // Força uma falha de validação comum caso o código da oferta seja fantasma
        assertThrows(ValidacaoException.class, () -> {
            turmaService.validarChoqueHorarioAluno(aluno, "DIREITO_01", periodo);
        });
    }
}