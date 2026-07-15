package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository; // IMPORT ADICIONADO[cite: 14]
import br.edu.uepb.classroompb.repository.PeriodoRepository; // US35: Import adicionado
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
    private MatriculaRepository matriculaRepository; // ATRIBUTO ADICIONADO[cite: 14]
    private PeriodoRepository periodoRepository; // US35: Atributo adicionado

    private static final String FILE_NOTAS = "data/notas.txt";
    private static final String FILE_TURMAS = "data/turmas.txt";
    private static final String FILE_MATRICULAS = "data/matriculas.txt"; // CAMINHO ADICIONADO[cite: 14]
    private static final String FILE_PERIODOS = "data/periodos.txt"; // US35: Caminho adicionado

    @Before
    public void setUp() throws Exception {
        // Garante a existência da pasta data
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Limpa rigorosamente os arquivos físicos para isolar cada caso de teste[cite: 14]
        new File(FILE_NOTAS).delete();
        new File(FILE_TURMAS).delete();
        new File(FILE_MATRICULAS).delete(); // DELETAR ARQUIVO DE MATRÍCULAS[cite: 14]
        new File(FILE_PERIODOS).delete(); // US35: DELETAR ARQUIVO DE PERÍODOS

        // Inicializa os repositórios reais[cite: 14]
        notaRepository = new NotaRepository();
        turmaRepository = new TurmaRepository();
        matriculaRepository = new MatriculaRepository(); // INSTANCIAÇÃO ADICIONADA[cite: 14]
        periodoRepository = new PeriodoRepository(); // US35: INSTANCIAÇÃO ADICIONADA

        // Inicializa o serviço sob teste com a nova dependência de período (US35)
        notaService = new NotaService(notaRepository, turmaRepository, matriculaRepository, periodoRepository);
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

    /**
     * TESTE [TASK 2526]: Garante que a busca por notas do período filtre e retorne
     * com precisão apenas as notas correspondentes ao aluno solicitado.
     */
    @Test
    public void deveRecuperarNotasComSucessoApenasDoAlunoEspecificado() throws Exception {
        String alunoAlvo = "20261001";
        String outroAluno = "20261002";
        String disciplina = "P1";
        String periodo = "2026.1";

        // Prepara turmas e vínculos de matrículas ativos para ambos os alunos[cite: 14]
        turmaRepository.salvar(new Turma(disciplina, "PROF_A", periodo, 30, "24M12", "Sala_101"));
        matriculaRepository.salvar(new br.edu.uepb.classroompb.model.Matricula(alunoAlvo, disciplina, periodo, br.edu.uepb.classroompb.model.Matricula.StatusMatricula.CONFIRMADA));
        matriculaRepository.salvar(new br.edu.uepb.classroompb.model.Matricula(outroAluno, disciplina, periodo, br.edu.uepb.classroompb.model.Matricula.StatusMatricula.CONFIRMADA));

        // Grava as notas individuais de cada estudante no repositório físico[cite: 5]
        notaRepository.salvar(new Nota(alunoAlvo, disciplina, periodo, 8.0, 9.0, -1.0));
        notaRepository.salvar(new Nota(outroAluno, disciplina, periodo, 5.0, 6.0, -1.0));

        // EXECUÇÃO: Busca o boletim do alunoAlvo[cite: 13]
        List<Nota> boletimAlunoAlvo = notaService.buscarNotasPorAlunoEPeriodo(alunoAlvo, periodo);

        // VERIFICAÇÃO: Deve conter apenas o registro do alunoAlvo com suas respectivas notas
        assertEquals(1, boletimAlunoAlvo.size());
        Nota notaRetornada = boletimAlunoAlvo.get(0);
        assertEquals(alunoAlvo, notaRetornada.getMatriculaAluno());
        assertEquals(8.0, notaRetornada.getNota1(), 0.01);
        assertEquals(9.0, notaRetornada.getNota2(), 0.01);
    }

    /**
     * TESTE [TASK 2526]: Garante o isolamento absoluto, comprovando que a consulta de notas
     * de um aluno não retorne ou exponha nenhum dado de notas pertencente a terceiros.
     */
    @Test
    public void deveGarantirIsolamentoEEvitarVazamentoDeNotasDeTerceiros() throws Exception {
        String alunoLogado = "20261001";
        String alunoEstranho = "20269999"; // Aluno de outra turma/contexto
        String disciplina = "P1";
        String periodo = "2026.1";

        // O aluno logado possui matrícula ativa na disciplina[cite: 14]
        turmaRepository.salvar(new Turma(disciplina, "PROF_A", periodo, 30, "24M12", "Sala_101"));
        matriculaRepository.salvar(new br.edu.uepb.classroompb.model.Matricula(alunoLogado, disciplina, periodo, br.edu.uepb.classroompb.model.Matricula.StatusMatricula.CONFIRMADA));

        // Há notas salvas de um terceiro no arquivo[cite: 5]
        notaRepository.salvar(new Nota(alunoEstranho, disciplina, periodo, 10.0, 10.0, -1.0));

        // EXECUÇÃO: O aluno logado consulta seu próprio boletim[cite: 13]
        List<Nota> boletim = notaService.buscarNotasPorAlunoEPeriodo(alunoLogado, periodo);

        // VERIFICAÇÃO: O sistema deve isolar e não pode vazar a nota de 10.0 do outro estudante.
        // Como o aluno logado não tem nota lançada, o serviço deve retornar uma nota padrão zerada para ele.[cite: 13]
        assertEquals(1, boletim.size());
        Nota notaRetornada = boletim.get(0);
        assertEquals(alunoLogado, notaRetornada.getMatriculaAluno());
        assertEquals(0.0, notaRetornada.getNota1(), 0.01); // Retorno seguro padrão zerado[cite: 13]
        assertNotEquals(10.0, notaRetornada.getNota1(), 0.01); // Nota de terceiro protegida
    }
}