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
  private final br.edu.uepb.classroompb.repository.AvaliacaoRepository avaliacaoRepository;
  private final br.edu.uepb.classroompb.repository.DiarioRepository diarioRepository;

  public SituacaoAcademicaService(
      NotaRepository notaRepository,
      FrequenciaService frequenciaService,
      br.edu.uepb.classroompb.repository.AvaliacaoRepository avaliacaoRepository,
      br.edu.uepb.classroompb.repository.DiarioRepository diarioRepository) {
    this.notaRepository = notaRepository;
    this.frequenciaService = frequenciaService;
    this.avaliacaoRepository = avaliacaoRepository;
    this.diarioRepository = diarioRepository;
  }

  /**
   * Calcula a média. Se houver avaliações cadastradas, faz a média ponderada. Se não houver, faz a
   * média aritmética simples considerando as etapas lançadas (se nota3 < 0 divide por 2).
   */
  public double calcularMedia(Nota nota) {
    br.edu.uepb.classroompb.model.Diario diario = null;
    for (br.edu.uepb.classroompb.model.Diario d : diarioRepository.buscarTodos()) {
      if (d.getCodigoDisciplina().equalsIgnoreCase(nota.getCodigoDisciplina())
          && d.getPeriodo().equalsIgnoreCase(nota.getPeriodo())) {
        diario = d;
        break;
      }
    }

    if (diario != null) {
      java.util.List<br.edu.uepb.classroompb.model.Avaliacao> avaliacoes =
          avaliacaoRepository.buscarPorDiario(diario.getCodigo());
      if (!avaliacoes.isEmpty()) {
        double somaPesos = 0.0;
        double somaNotasPonderadas = 0.0;

        for (br.edu.uepb.classroompb.model.Avaliacao av : avaliacoes) {
          double notaValor = -1.0;
          if (av.getEtapa() == 1) notaValor = nota.getNota1();
          else if (av.getEtapa() == 2) notaValor = nota.getNota2();
          else if (av.getEtapa() == 3) notaValor = nota.getNota3();

          if (notaValor >= 0) {
            double notaEscaladaPara10 = (notaValor / av.getNotaMaxima()) * 10.0;
            somaNotasPonderadas += notaEscaladaPara10 * av.getPeso();
            somaPesos += av.getPeso();
          }
        }
        if (somaPesos > 0) {
          return somaNotasPonderadas / somaPesos;
        }
      }
    }

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
