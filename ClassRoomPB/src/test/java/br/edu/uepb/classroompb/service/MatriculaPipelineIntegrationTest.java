package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Periodo;
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
import java.util.List;

import static org.junit.Assert.*;

public class MatriculaPipelineIntegrationTest {

    private TurmaService turmaService;
    private TurmaRepository turmaRepository;
    private PeriodoRepository periodoRepository;
    private DisciplinaRepository disciplinaRepository;
    private MatriculaRepository matriculaRepository;

    private static final String FILE_TURMAS = "data/turmas.txt";
    private static final String FILE_MATRICULAS = "data/matriculas.txt";

    @Before
    public void setUp() throws Exception {
        // 1. Limpa os arquivos físicos locais para garantir um ambiente de teste isolado e limpo
        File fTurmas = new File(FILE_TURMAS);
        if (fTurmas.exists()) fTurmas.delete();

        File fMatriculas = new File(FILE_MATRICULAS);
        if (fMatriculas.exists()) fMatriculas.delete();

        // 2. Inicializa os repositórios reais do ecossistema do projeto
        turmaRepository = new TurmaRepository();
        periodoRepository = new PeriodoRepository();
        disciplinaRepository = new DisciplinaRepository();
        matriculaRepository = new MatriculaRepository();

        // 3. Inicializa o serviço centralizador que contém o pipeline orquestrador
        turmaService = new TurmaService(turmaRepository, periodoRepository, disciplinaRepository);
    }

    /**
     * FLUXO DE SUCESSO: Testa se o pipeline valida todos os critérios e consolida 
     * a matrícula de forma automática como CONFIRMADA em disco.
     */
    @Test
    public void deveConsolidarMatriculaComoConfirmadaNoFluxOFechadoDeSucesso() throws Exception {
        String aluno = "20262001";
        String periodoCodigo = "2026.1";
        String disciplina = "P1"; // Disciplina simulada sem pré-requisitos impeditivos

        // CORREÇÃO: Passando apenas 2 parâmetros e o status "INICIADO" para abrir as matrículas
        periodoRepository.salvar(new Periodo(periodoCodigo, "INICIADO"));

        // Cadastra uma turma com vagas disponíveis (vagas: 30, ocupadas: 0)
        turmaRepository.salvar(new Turma(disciplina, "PROF_A", periodoCodigo, 30, "24M12", "Sala_101"));

        // EXECUÇÃO: Dispara o pipeline automático
        turmaService.processarMatriculaAutomatica(aluno, disciplina, periodoCodigo);

        // VERIFICAÇÃO 1: O contador de vagas ocupadas da turma deve ter incrementado para 1
        List<Turma> turmasEmDisco = turmaRepository.buscarTodas();
        assertEquals(1, turmasEmDisco.size());
        assertEquals(1, turmasEmDisco.get(0).getVagasOcupadas());

        // VERIFICAÇÃO 2: O registro de matrícula deve ter nascido diretamente como CONFIRMADA no arquivo texto
        List<Matricula> matriculasEmDisco = matriculaRepository.buscarTodas();
        assertEquals(1, matriculasEmDisco.size());
        
        Matricula matriculaSalva = matriculasEmDisco.get(0);
        assertEquals(aluno, matriculaSalva.getMatriculaAluno());
        assertEquals(disciplina, matriculaSalva.getCodigoDisciplina());
        assertEquals(Matricula.StatusMatricula.CONFIRMADA, matriculaSalva.getStatus());
    }

    /**
     * FLUXO ALTERNATIVO (FALHA DE PERÍODO): Testa se o pipeline aborta caso o período letivo 
     * não esteja aberto para matrículas, não alterando os dados das turmas ou matrículas.
     */
    @Test
    public void deveAbortarPipelineENaoAlterarArquivosSeOPeriodoEstiverFechado() throws Exception {
        String aluno = "20262002";
        String periodoCodigo = "2026.1";
        String disciplina = "P1";

        // CORREÇÃO: Passando 2 parâmetros e o status "PLANEJADO" (fechado para matrículas)
        periodoRepository.salvar(new Periodo(periodoCodigo, "PLANEJADO"));
        turmaRepository.salvar(new Turma(disciplina, "PROF_A", periodoCodigo, 30, "24M12", "Sala_101"));

        // EXECUÇÃO E ASSERÇÃO: Deve lançar ValidacaoException
        assertThrows(ValidacaoException.class, () -> {
            turmaService.processarMatriculaAutomatica(aluno, disciplina, periodoCodigo);
        });

        // VERIFICAÇÃO: Os arquivos devem permanecer intocados (Vagas ocupadas continua 0 e nenhuma matrícula salva)
        assertEquals(0, turmaRepository.buscarTodas().get(0).getVagasOcupadas());
        assertTrue(matriculaRepository.buscarTodas().isEmpty());
    }

    /**
     * FLUXO ALTERNATIVO (FALHA DE VAGAS): Testa se o pipeline bloqueia e não gera matrícula 
     * caso o teto físico de vagas da turma tenha sido atingido.
     */
    @Test
    public void deveAbortarPipelineENaoAlterarArquivosSeATurmaNaoTiverVagasDisponiveis() throws Exception {
        String aluno = "20262003";
        String periodoCodigo = "2026.1";
        String disciplina = "P1";

        // CORREÇÃO: Passando 2 parâmetros e o status "INICIADO" para o período permitir o fluxo passar
        periodoRepository.salvar(new Periodo(periodoCodigo, "INICIADO"));
        
        // Configura uma turma lotada (Vagas: 10, Ocupadas: 10)
        turmaRepository.salvar(new Turma(disciplina, "PROF_A", periodoCodigo, 10, 10, "24M12", "Sala_101"));

        // EXECUÇÃO E ASSERÇÃO: Deve lançar ValidacaoException por falta de vagas
        assertThrows(ValidacaoException.class, () -> {
            turmaService.processarMatriculaAutomatica(aluno, disciplina, periodoCodigo);
        });

        // VERIFICAÇÃO: O arquivo de turmas permaneceu com 10 vagas ocupadas e nenhuma matrícula foi registrada
        assertEquals(10, turmaRepository.buscarTodas().get(0).getVagasOcupadas());
        assertTrue(matriculaRepository.buscarTodas().isEmpty());
    }

    /**
     * FLUXO ALTERNATIVO (CHOQUE DE HORÁRIO): Testa se o pipeline aborta caso o motor antichoques
     * detecte uma colisão de horários na grade do aluno, protegendo a integridade dos dados.
     */
    @Test
    public void deveAbortarPipelineENaoGerarMatriculaSeOAlunoApresentarChoqueDeHorario() throws Exception {
        String aluno = "20262004";
        String periodoCodigo = "2026.1";

        // CORREÇÃO: Passando 2 parâmetros e o status "INICIADO" para o período permitir o fluxo passar
        periodoRepository.salvar(new Periodo(periodoCodigo, "INICIADO"));
        
        // Cadastra duas turmas diferentes ocorrendo no MESMO horário ("24M12")
        turmaRepository.salvar(new Turma("ES01", "PROF_A", periodoCodigo, 40, "24M12", "Sala_101"));
        turmaRepository.salvar(new Turma("BD01", "PROF_B", periodoCodigo, 40, "24M12", "Sala_102"));

        // Simula no arquivo texto que o aluno já conquistou uma matrícula CONFIRMADA na primeira turma (ES01)
        matriculaRepository.salvar(new Matricula(aluno, "ES01", periodoCodigo, Matricula.StatusMatricula.CONFIRMADA));

        // EXECUÇÃO E ASSERÇÃO: Tentar matricular na segunda turma (BD01) deve estourar o Choque de Horários
        assertThrows(ChoqueHorarioAlunoException.class, () -> {
            turmaService.processarMatriculaAutomatica(aluno, "BD01", periodoCodigo);
        });

        // VERIFICAÇÃO: A vaga da turma de BD01 não pode ter sido incrementada (permanece 0) 
        // e o arquivo de matrículas continua apenas com o registro inicial de ES01 (tamanho 1)
        for (Turma t : turmaRepository.buscarTodas()) {
            if (t.getCodigoDisciplina().equalsIgnoreCase("BD01")) {
                assertEquals(0, t.getVagasOcupadas());
            }
        }
        assertEquals(1, matriculaRepository.buscarTodas().size());
    }
}