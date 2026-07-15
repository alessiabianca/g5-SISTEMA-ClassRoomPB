package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class NotaServiceTest {

    private NotaService notaService;
    private NotaRepository notaRepository;
    private TurmaRepository turmaRepository;

    private static final String FILE_NOTAS = "data/notas.txt";
    private static final String FILE_TURMAS = "data/turmas.txt";

    @Before
    public void setUp() throws Exception {
        // Garante a existência da pasta data
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Limpa rigorosamente os arquivos físicos para isolar cada caso de teste
        new File(FILE_NOTAS).delete();
        new File(FILE_TURMAS).delete();

        // Inicializa os repositórios reais
        notaRepository = new NotaRepository();
        turmaRepository = new TurmaRepository();

        // Inicializa o serviço sob teste
        notaService = new NotaService(notaRepository, turmaRepository);
    }

    /**
     * FLUXO DE SUCESSO: Garante que o professor responsável consiga lançar
     * uma nota válida (no intervalo de 0.0 a 10.0) com sucesso.
     */
    @Test
    public void deveLancarNotaComSucessoParaProfessorResponsavel() throws Exception {
        String professorResponsavel = "PROF_A";
        String aluno = "20261001";
        String disciplina = "P1";
        String periodo = "2026.1";

        // Cadastra a turma associada ao PROF_A
        turmaRepository.salvar(new Turma(disciplina, professorResponsavel, periodo, 30, "24M12", "Sala_101"));

        // EXECUÇÃO: Lança a nota da 1ª Etapa
        notaService.lancarNota(professorResponsavel, aluno, disciplina, periodo, 1, 8.5);

        // VERIFICAÇÃO: A nota deve ter sido salva e recuperada corretamente do disco
        Nota notaSalva = notaRepository.buscarPorAlunoEDisciplina(aluno, disciplina, periodo);
        assertNotNull(notaSalva);
        assertEquals(8.5, notaSalva.getNota1(), 0.01);
    }

    /**
     * FLUXO DE FALHA (NOTA NEGATIVA): Garante o bloqueio de lançamento 
     * de notas abaixo de 0.0, estourando ValidacaoException.
     */
    @Test
    public void deveLancarValidacaoExceptionParaNotaNegativa() throws Exception {
        String professorResponsavel = "PROF_A";
        String aluno = "20261002";
        String disciplina = "P1";
        String periodo = "2026.1";

        turmaRepository.salvar(new Turma(disciplina, professorResponsavel, periodo, 30, "24M12", "Sala_101"));

        // EXECUÇÃO E ASSERÇÃO: Deve lançar erro para nota -1.5
        assertThrows(ValidacaoException.class, () -> {
            notaService.lancarNota(professorResponsavel, aluno, disciplina, periodo, 1, -1.5);
        });

        // VERIFICAÇÃO: Nenhum registro de nota pode ter sido criado em disco
        assertTrue(notaRepository.buscarTodas().isEmpty());
    }

    /**
     * FLUXO DE FALHA (NOTA MAIOR QUE DEZ): Garante o bloqueio de lançamento 
     * de notas acima de 10.0, estourando ValidacaoException.
     */
    @Test
    public void deveLancarValidacaoExceptionParaNotaMaiorQueDez() throws Exception {
        String professorResponsavel = "PROF_A";
        String aluno = "20261003";
        String disciplina = "P1";
        String periodo = "2026.1";

        turmaRepository.salvar(new Turma(disciplina, professorResponsavel, periodo, 30, "24M12", "Sala_101"));

        // EXECUÇÃO E ASSERÇÃO: Deve lançar erro para nota 10.5
        assertThrows(ValidacaoException.class, () -> {
            notaService.lancarNota(professorResponsavel, aluno, disciplina, periodo, 1, 10.5);
        });

        // VERIFICAÇÃO: Nenhum registro de nota pode ter sido criado em disco
        assertTrue(notaRepository.buscarTodas().isEmpty());
    }

    /**
     * FLUXO DE FALHA (PERMISSÃO / SEGURANÇA): Garante que um professor 
     * não associado à turma seja bloqueado de lançar notas para os alunos dela.
     */
    @Test
    public void deveLancarValidacaoExceptionParaProfessorNaoResponsavel() throws Exception {
        String professorResponsavel = "PROF_A";
        String professorInvasor = "PROF_B";
        String aluno = "20261004";
        String disciplina = "P1";
        String periodo = "2026.1";

        // Turma é do PROF_A
        turmaRepository.salvar(new Turma(disciplina, professorResponsavel, periodo, 30, "24M12", "Sala_101"));

        // EXECUÇÃO E ASSERÇÃO: PROF_B tenta lançar nota e deve ser bloqueado
        assertThrows(ValidacaoException.class, () -> {
            notaService.lancarNota(professorInvasor, aluno, disciplina, periodo, 1, 9.0);
        });

        // VERIFICAÇÃO: O arquivo de notas deve permanecer vazio
        assertTrue(notaRepository.buscarTodas().isEmpty());
    }
}