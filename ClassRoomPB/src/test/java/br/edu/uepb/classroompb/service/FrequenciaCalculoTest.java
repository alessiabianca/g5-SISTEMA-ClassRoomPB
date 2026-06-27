package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.DesempenhoFrequencia;
import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class FrequenciaCalculoTest {

    private FrequenciaService frequenciaService;
    private TurmaRepository turmaRepository;
    private MatriculaRepository matriculaRepository;
    private FrequenciaRepository frequenciaRepository;

    private static final String FILE_TURMAS = "data/turmas.txt";
    private static final String FILE_FREQUENCIAS = "data/frequencias.txt";

    @Before
    public void setUp() throws Exception {
        File fTurmas = new File(FILE_TURMAS);
        if (fTurmas.exists()) fTurmas.delete();

        File fFrequencias = new File(FILE_FREQUENCIAS);
        if (fFrequencias.exists()) fFrequencias.delete();

        turmaRepository = new TurmaRepository();
        matriculaRepository = new MatriculaRepository();
        frequenciaRepository = new FrequenciaRepository();

        frequenciaService = new FrequenciaService(turmaRepository, matriculaRepository, frequenciaRepository);
    }

    /**
     * CRITÉRIO DE MARGEM: Se a turma existe mas nenhuma aula foi registrada ainda, 
     * o motor deve computar 100% por padrão regulamentar para evitar divisão por zero.
     */
    @Test
    public void deveRetornarCemPorCentoQuandoNaoHouverNenhumaAulaRegistrada() throws Exception {
        turmaRepository.salvar(new Turma("ES01", "PROF_42", "2026.1", 40, "24M12", "Sala 1"));

        DesempenhoFrequencia resultado = frequenciaService.calcularPercentualFrequencia("2026100", "ES01", "2026.1");
        
        assertEquals(0, resultado.getTotalAulas());
        assertEquals(100.0, resultado.getPercentualFrequencia(), 0.01);
    }

    /**
     * CRITÉRIO MATEMÁTICO DE PRECISÃO: Valida se o motor computa corretamente frações de frequência 
     * usando pontos flutuantes de forma precisa (Ex: 4 aulas, 3 presenças = 75.0%).
     */
    @Test
    public void deveCalcularPercentualExatoComFaltasAcumuladas() throws Exception {
        String aluno = "2026100";
        String disciplina = "ES01";
        String periodo = "2026.1";

        turmaRepository.salvar(new Turma(disciplina, "PROF_42", periodo, 40, "24M12", "Sala 1"));

        // Simula 4 aulas gravadas: 3 presenças e 1 falta (Aproveitamento esperado: 75%)
        List<Frequencia> aulas = new ArrayList<>();
        aulas.add(new Frequencia("01/06/2026", aluno, disciplina, periodo, Frequencia.TipoFrequencia.PRESENCA));
        aulas.add(new Frequencia("03/06/2026", aluno, disciplina, periodo, Frequencia.TipoFrequencia.PRESENCA));
        aulas.add(new Frequencia("05/06/2026", aluno, disciplina, periodo, Frequencia.TipoFrequencia.PRESENCA));
        aulas.add(new Frequencia("08/06/2026", aluno, disciplina, periodo, Frequencia.TipoFrequencia.FALTA));
        frequenciaRepository.salvarLote(aulas);

        DesempenhoFrequencia resultado = frequenciaService.calcularPercentualFrequencia(aluno, disciplina, periodo);

        assertEquals(4, resultado.getTotalAulas());
        assertEquals(3, resultado.getPresencas());
        assertEquals(1, resultado.getFaltas());
        assertEquals(75.0, resultado.getPercentualFrequencia(), 0.01);
    }
}