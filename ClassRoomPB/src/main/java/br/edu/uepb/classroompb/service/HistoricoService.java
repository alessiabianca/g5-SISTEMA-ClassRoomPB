package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.DesempenhoFrequencia;
import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

import java.util.ArrayList;
import java.util.List;

public class HistoricoService {
    private final HistoricoRepository historicoRepository;
    private final MatriculaRepository matriculaRepository;
    private final SituacaoAcademicaService situacaoService;
    private final FrequenciaService frequenciaService;

    public HistoricoService(HistoricoRepository historicoRepository, MatriculaRepository matriculaRepository,
                            SituacaoAcademicaService situacaoService, FrequenciaService frequenciaService) {
        this.historicoRepository = historicoRepository;
        this.matriculaRepository = matriculaRepository;
        this.situacaoService = situacaoService;
        this.frequenciaService = frequenciaService;
    }

    public void gerarHistoricoDoPeriodo(String periodo) {
        List<Matricula> matriculas = matriculaRepository.buscarTodas();
        List<Historico> historicosParaSalvar = new ArrayList<>();

        for (Matricula m : matriculas) {
            if (m.getPeriodo().equalsIgnoreCase(periodo) && m.getStatus() == Matricula.StatusMatricula.CONFIRMADA) {
                String aluno = m.getMatriculaAluno();
                String disc = m.getCodigoDisciplina();
                
                double media = 0.0;
                double freqPercent = 0.0;
                StatusAcademico status;
                
                try {
                    SituacaoAcademicaService.ResultadoApuracao resultado = situacaoService.apurarSituacao(aluno, disc, periodo);
                    media = resultado.getMedia();
                    freqPercent = resultado.getDesempenho().getPercentualFrequencia();
                    status = resultado.getStatus();
                } catch (ValidacaoException e) {
                    // Trata o cenário onde a nota não foi lançada
                    try {
                        DesempenhoFrequencia desemp = frequenciaService.calcularPercentualFrequencia(aluno, disc, periodo);
                        freqPercent = desemp.getPercentualFrequencia();
                    } catch (ValidacaoException ex) {
                        freqPercent = 0.0;
                    }
                    media = 0.0;
                    status = situacaoService.avaliarStatus(media, freqPercent);
                }

                Historico h = new Historico(aluno, disc, periodo, media, freqPercent, status);
                historicosParaSalvar.add(h);
            }
        }

        if (!historicosParaSalvar.isEmpty()) {
            historicoRepository.salvarLote(historicosParaSalvar);
        }
    }

    public List<Historico> consultarHistorico(String matriculaAluno) {
        return historicoRepository.buscarPorAluno(matriculaAluno);
    }
}
