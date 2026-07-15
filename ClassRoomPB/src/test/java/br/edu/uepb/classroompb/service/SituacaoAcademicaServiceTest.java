package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * US34 — Testes unitários das transições de status acadêmico e limites de faltas/notas.
 * Segue o padrão de teste do projeto (JUnit 4, @Before com limpeza de arquivos, repositórios reais).
 */
public class SituacaoAcademicaServiceTest {

    private SituacaoAcademicaService situacaoService;
    private NotaRepository notaRepository;
    private FrequenciaService frequenciaService;
    private TurmaRepository turmaRepository;
    private MatriculaRepository matriculaRepository;
    private FrequenciaRepository frequenciaRepository;

    private static final String FILE_TURMAS = "data/turmas.txt";
    private static final String FILE_FREQUENCIAS = "data/frequencias.txt";
    private static final String FILE_NOTAS = "data/notas.txt";
    private static final String FILE_MATRICULAS = "data/matriculas.txt";

    private static final String ALUNO = "2026100";
    private static final String DISCIPLINA = "ES01";
    private static final String PERIODO = "2026.1";

    @Before
    public void setUp() throws Exception {
        // Limpeza dos arquivos de dados para garantir isolamento entre testes
        File fTurmas = new File(FILE_TURMAS);
        if (fTurmas.exists()) fTurmas.delete();
        File fFrequencias = new File(FILE_FREQUENCIAS);
        if (fFrequencias.exists()) fFrequencias.delete();
        File fNotas = new File(FILE_NOTAS);
        if (fNotas.exists()) fNotas.delete();
        File fMatriculas = new File(FILE_MATRICULAS);
        if (fMatriculas.exists()) fMatriculas.delete();

        turmaRepository = new TurmaRepository();
        matriculaRepository = new MatriculaRepository();
        frequenciaRepository = new FrequenciaRepository();
        notaRepository = new NotaRepository();
        frequenciaService = new FrequenciaService(turmaRepository, matriculaRepository, frequenciaRepository, notaRepository);
        situacaoService = new SituacaoAcademicaService(notaRepository, frequenciaService);

        // Cadastra a turma base utilizada por todos os cenários
        turmaRepository.salvar(new Turma(DISCIPLINA, "PROF_42", PERIODO, 40, "24M12", "Sala 1"));
    }

    // ====================================================================
    // TESTES DO MÉTODO PURO avaliarStatus (sem I/O)
    // ====================================================================

    @Test
    public void deveRetornarAprovadoQuandoMediaAcimaDeSeteEFrequenciaSuficiente() {
        StatusAcademico resultado = situacaoService.avaliarStatus(8.5, 90.0);
        assertEquals(StatusAcademico.APROVADO, resultado);
    }

    @Test
    public void deveRetornarRecuperacaoQuandoMediaEntrQuatroESeteComFrequenciaSuficiente() {
        StatusAcademico resultado = situacaoService.avaliarStatus(5.5, 80.0);
        assertEquals(StatusAcademico.RECUPERACAO, resultado);
    }

    @Test
    public void deveRetornarReprovadoNotaQuandoMediaAbaixoDeQuatroComFrequenciaSuficiente() {
        StatusAcademico resultado = situacaoService.avaliarStatus(3.0, 85.0);
        assertEquals(StatusAcademico.REPROVADO_NOTA, resultado);
    }

    @Test
    public void deveRetornarReprovadoFaltaQuandoFrequenciaInsuficienteIndependenteDaNota() {
        // Mesmo com nota alta, a frequência insuficiente deve prevalecer
        StatusAcademico resultado = situacaoService.avaliarStatus(9.0, 70.0);
        assertEquals(StatusAcademico.REPROVADO_FALTA, resultado);
    }

    // ====================================================================
    // TESTES DE LIMITES EXATOS (boundary values)
    // ====================================================================

    @Test
    public void deveRetornarAprovadoNoLimiteExatoDaMediaSete() {
        StatusAcademico resultado = situacaoService.avaliarStatus(7.0, 80.0);
        assertEquals(StatusAcademico.APROVADO, resultado);
    }

    @Test
    public void deveRetornarRecuperacaoNoLimiteExatoDaMediaQuatro() {
        StatusAcademico resultado = situacaoService.avaliarStatus(4.0, 80.0);
        assertEquals(StatusAcademico.RECUPERACAO, resultado);
    }

    @Test
    public void naoDeveReprovarPorFaltaNoLimiteExatoDeSetentaECincoPorCento() {
        // 75.0% é o mínimo aceitável — NÃO deve reprovar por falta
        StatusAcademico resultado = situacaoService.avaliarStatus(8.0, 75.0);
        assertEquals(StatusAcademico.APROVADO, resultado);
    }

    @Test
    public void deveReprovarPorFaltaComFrequenciaDeSetentaEQuatroVirgulaNove() {
        StatusAcademico resultado = situacaoService.avaliarStatus(8.0, 74.9);
        assertEquals(StatusAcademico.REPROVADO_FALTA, resultado);
    }

    // ====================================================================
    // TESTE DE INTEGRAÇÃO COMPLETA (com I/O em disco)
    // ====================================================================

    @Test
    public void deveApurarSituacaoCompletaComDadosReaisDeDisco() throws Exception {
        // Cenário: aluno com notas boas (média 7.5) e 80% de frequência → APROVADO

        // 1. Cadastra notas do aluno
        notaRepository.salvar(new Nota(ALUNO, DISCIPLINA, PERIODO, 8.0, 7.0, 7.5));

        // 2. Cadastra frequência: 4 presenças e 1 falta (80%)
        List<Frequencia> aulas = new ArrayList<>();
        aulas.add(new Frequencia("01/06/2026", ALUNO, DISCIPLINA, PERIODO, Frequencia.TipoFrequencia.PRESENCA));
        aulas.add(new Frequencia("03/06/2026", ALUNO, DISCIPLINA, PERIODO, Frequencia.TipoFrequencia.PRESENCA));
        aulas.add(new Frequencia("05/06/2026", ALUNO, DISCIPLINA, PERIODO, Frequencia.TipoFrequencia.PRESENCA));
        aulas.add(new Frequencia("08/06/2026", ALUNO, DISCIPLINA, PERIODO, Frequencia.TipoFrequencia.PRESENCA));
        aulas.add(new Frequencia("10/06/2026", ALUNO, DISCIPLINA, PERIODO, Frequencia.TipoFrequencia.FALTA));
        frequenciaRepository.salvarLote(aulas);

        // 3. Executa a apuração completa
        SituacaoAcademicaService.ResultadoApuracao resultado = situacaoService.apurarSituacao(ALUNO, DISCIPLINA, PERIODO);

        // 4. Validações
        assertEquals(StatusAcademico.APROVADO, resultado.getStatus());
        assertEquals(7.5, resultado.getMedia(), 0.01);
        assertEquals(80.0, resultado.getDesempenho().getPercentualFrequencia(), 0.01);
    }
}
