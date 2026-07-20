package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.DesempenhoFrequencia;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

/**
 * US34 — Serviço responsável pela apuração automatizada da situação acadêmica do aluno. Centraliza
 * a regra de negócio que cruza notas e frequência para determinar o status final.
 */
public class SituacaoAcademicaService {
  private final NotaRepository notaRepository;
  private final FrequenciaService frequenciaService;

  public SituacaoAcademicaService(
      NotaRepository notaRepository, FrequenciaService frequenciaService) {
    this.notaRepository = notaRepository;
    this.frequenciaService = frequenciaService;
  }

  /**
   * Calcula a média aritmética das notas de uma avaliação. Se a nota3 for negativa (-1), considera
   * apenas nota1 e nota2.
   */
  public double calcularMedia(Nota nota) {
    if (nota.getNota3() < 0) {
      return (nota.getNota1() + nota.getNota2()) / 2.0;
    }
    return (nota.getNota1() + nota.getNota2() + nota.getNota3()) / 3.0;
  }

  /**
   * Método puro de decisão (sem I/O) — aplica a tabela de regras de negócio.
   *
   * <p>Regras (US34): 1. Frequência < 75% → REPROVADO_FALTA (prioridade máxima) 2. Frequência ≥ 75%
   * e Média ≥ 7.0 → APROVADO 3. Frequência ≥ 75% e Média ≥ 4.0 e < 7.0 → RECUPERAÇÃO 4. Frequência
   * ≥ 75% e Média < 4.0 → REPROVADO_NOTA
   */
  public StatusAcademico avaliarStatus(double media, double percentualFrequencia) {

    if (percentualFrequencia < 75.0) {
      return StatusAcademico.REPROVADO_FALTA;
    }

    if (media >= 7.0) {
      return StatusAcademico.APROVADO;
    }

    if (media >= 4.0) {
      return StatusAcademico.RECUPERACAO;
    }

    return StatusAcademico.REPROVADO_NOTA;
  }

  /**
   * Método orquestrador de ponta a ponta: busca dados, calcula métricas e retorna o resultado.
   *
   * @return ResultadoApuracao contendo todos os dados consolidados para exibição no CLI.
   * @throws ValidacaoException se a nota ou frequência não forem encontradas.
   */
  public ResultadoApuracao apurarSituacao(
      String matriculaAluno, String codigoDisciplina, String periodo) throws ValidacaoException {

    Nota nota = notaRepository.buscarPorAlunoEDisciplina(matriculaAluno, codigoDisciplina, periodo);
    if (nota == null) {
      throw new ValidacaoException(
          "Erro: Nenhum registro de notas encontrado para o aluno '"
              + matriculaAluno
              + "' na disciplina '"
              + codigoDisciplina
              + "' no período '"
              + periodo
              + "'.");
    }

    double media = calcularMedia(nota);

    DesempenhoFrequencia desempenho =
        frequenciaService.calcularPercentualFrequencia(matriculaAluno, codigoDisciplina, periodo);

    StatusAcademico status = avaliarStatus(media, desempenho.getPercentualFrequencia());

    return new ResultadoApuracao(nota, media, desempenho, status);
  }

  /**
   * DTO interno que encapsula todos os dados consolidados de uma apuração acadêmica. Facilita a
   * exibição formatada no CLI sem expor detalhes internos do service.
   */
  public static class ResultadoApuracao {
    private final Nota nota;
    private final double media;
    private final DesempenhoFrequencia desempenho;
    private final StatusAcademico status;

    public ResultadoApuracao(
        Nota nota, double media, DesempenhoFrequencia desempenho, StatusAcademico status) {
      this.nota = nota;
      this.media = media;
      this.desempenho = desempenho;
      this.status = status;
    }

    public Nota getNota() {
      return nota;
    }

    public double getMedia() {
      return media;
    }

    public DesempenhoFrequencia getDesempenho() {
      return desempenho;
    }

    public StatusAcademico getStatus() {
      return status;
    }
  }
}
