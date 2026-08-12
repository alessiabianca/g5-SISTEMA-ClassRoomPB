package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Avaliacao;
import br.edu.uepb.classroompb.model.DesempenhoFrequencia;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.RelatorioReprovacaoDisciplina;
import br.edu.uepb.classroompb.model.ReprovacaoDisciplina;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.AvaliacaoRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoricoService {

  private final HistoricoRepository historicoRepository;
  private final MatriculaRepository matriculaRepository;
  private final SituacaoAcademicaService situacaoService;
  private final FrequenciaService frequenciaService;
  private final DiarioRepository diarioRepository;
  private final AvaliacaoRepository avaliacaoRepository;
  private final NotaRepository notaRepository;
  private final FrequenciaRepository frequenciaRepository;

  public HistoricoService(
      HistoricoRepository historicoRepository,
      MatriculaRepository matriculaRepository,
      TurmaRepository turmaRepository,
      SituacaoAcademicaService situacaoService,
      FrequenciaService frequenciaService) {
    this(
        historicoRepository,
        matriculaRepository,
        turmaRepository,
        situacaoService,
        frequenciaService,
        new DiarioRepository(),
        new AvaliacaoRepository(),
        new NotaRepository(),
        new FrequenciaRepository());
  }

  public HistoricoService(
      HistoricoRepository historicoRepository,
      MatriculaRepository matriculaRepository,
      TurmaRepository turmaRepository,
      SituacaoAcademicaService situacaoService,
      FrequenciaService frequenciaService,
      DiarioRepository diarioRepository,
      AvaliacaoRepository avaliacaoRepository,
      NotaRepository notaRepository,
      FrequenciaRepository frequenciaRepository) {
    this.historicoRepository = historicoRepository;
    this.matriculaRepository = matriculaRepository;
    this.situacaoService = situacaoService;
    this.frequenciaService = frequenciaService;
    this.diarioRepository = diarioRepository;
    this.avaliacaoRepository = avaliacaoRepository;
    this.notaRepository = notaRepository;
    this.frequenciaRepository = frequenciaRepository;
  }

  public void gerarHistoricoDoPeriodo(String periodo) {
    List<Matricula> matriculas = matriculaRepository.buscarTodas();
    List<Historico> historicosParaSalvar = new ArrayList<>();
    Set<String> chavesConsolidadas = carregarChavesConsolidadas(periodo);

    for (Matricula m : matriculas) {
      if (m.getPeriodo().equalsIgnoreCase(periodo)
          && m.getStatus() == Matricula.StatusMatricula.CONFIRMADA) {
        String aluno = m.getMatriculaAluno();
        String disc = m.getCodigoDisciplina();
        String chaveHistorico = montarChaveHistorico(aluno, disc, periodo);

        if (!chavesConsolidadas.contains(chaveHistorico)) {
          historicosParaSalvar.add(consolidarResultadoFinalTurma(aluno, disc, periodo));
          chavesConsolidadas.add(chaveHistorico);
        }
      }
    }

    if (!historicosParaSalvar.isEmpty()) {
      historicoRepository.salvarLote(historicosParaSalvar);
    }
  }

  public List<Historico> consultarHistorico(String matriculaAluno) {
    List<Historico> historico = new ArrayList<>(historicoRepository.buscarPorAluno(matriculaAluno));
    historico.sort(
        Comparator.comparing(Historico::getPeriodo, this::compararPeriodos)
            .thenComparing(Historico::getCodigoDisciplina, String.CASE_INSENSITIVE_ORDER));
    return historico;
  }

  public List<Historico> consultarHistoricoAluno(
      String matriculaAluno, UsuarioRepository usuarioRepository) throws ValidacaoException {
    Usuario aluno = usuarioRepository.buscarPorMatricula(matriculaAluno);
    if (aluno == null || !"ALUNO".equalsIgnoreCase(aluno.getPerfil())) {
      throw new ValidacaoException("Aluno nao encontrado para a matricula informada.");
    }
    return consultarHistorico(aluno.getMatricula());
  }

  public List<Historico> consultarHistoricoAluno(
      String matriculaAluno, UsuarioRepository usuarioRepository, String codigoCursoCoordenador)
      throws ValidacaoException {
    Usuario aluno = usuarioRepository.buscarPorMatricula(matriculaAluno);
    if (aluno == null || !"ALUNO".equalsIgnoreCase(aluno.getPerfil())) {
      throw new ValidacaoException("Aluno nao encontrado para a matricula informada.");
    }
    if (codigoCursoCoordenador == null
        || codigoCursoCoordenador.isBlank()
        || aluno.getCodigoCurso() == null
        || aluno.getCodigoCurso().isBlank()
        || !codigoCursoCoordenador.equalsIgnoreCase(aluno.getCodigoCurso())) {
      throw new ValidacaoException("Acesso negado: aluno nao vinculado ao curso do coordenador.");
    }
    return consultarHistorico(aluno.getMatricula());
  }

  public String formatarHistorico(List<Historico> historico) {
    StringBuilder linhasFormatadas = new StringBuilder();
    for (Historico registro : historico) {
      String frequenciaFormatada =
          String.format(Locale.US, "%.1f%%", registro.getPercentualFrequencia());
      linhasFormatadas.append(
          String.format(
              Locale.US,
              " %-12s | %-12s | %-16s | %-7.1f | %-7s | %-25s%n",
              registro.getPeriodo(),
              registro.getCodigoDisciplina().toUpperCase(),
              registro.getMatriculaProfessor(),
              registro.getMediaFinal(),
              frequenciaFormatada,
              registro.getStatus().name()));
    }
    return linhasFormatadas.toString();
  }

  /** RF42: Gera o relatorio de reprovacao consolidado por disciplina. */
  public RelatorioReprovacaoDisciplina gerarRelatorioReprovacaoPorDisciplina() {
    return new RelatorioReprovacaoDisciplinaService(historicoRepository)
        .gerarRelatorioReprovacaoPorDisciplina();
  }

  /** RF42: Gera o relatorio de reprovacao de uma disciplina especifica. */
  public RelatorioReprovacaoDisciplina gerarRelatorioReprovacaoPorDisciplina(
      String codigoDisciplina) throws ValidacaoException {
    return new RelatorioReprovacaoDisciplinaService(historicoRepository)
        .gerarRelatorioReprovacaoPorDisciplina(codigoDisciplina);
  }

  /** RF42: Calcula os indicadores de reprovacao de uma disciplina especifica. */
  public ReprovacaoDisciplina calcularReprovacaoDisciplina(String codigoDisciplina)
      throws ValidacaoException {
    return new RelatorioReprovacaoDisciplinaService(historicoRepository)
        .calcularReprovacaoDisciplina(codigoDisciplina);
  }

  private Historico consolidarResultadoFinalTurma(
      String aluno, String codigoDisciplina, String periodo) {
    List<Diario> diarios = buscarDiariosFechados(codigoDisciplina, periodo);
    if (!diarios.isEmpty()) {
      return consolidarDiarios(aluno, codigoDisciplina, periodo, diarios);
    }

    String matriculaProfessor = "N/A";
    double media = 0.0;
    double freqPercent = 0.0;
    StatusAcademico status;

    try {
      SituacaoAcademicaService.ResultadoApuracao resultado =
          situacaoService.apurarSituacao(aluno, codigoDisciplina, periodo);
      media = resultado.getMedia();
      freqPercent = resultado.getDesempenho().getPercentualFrequencia();
      status = resultado.getStatus();
    } catch (ValidacaoException e) {

      try {
        DesempenhoFrequencia desemp =
            frequenciaService.calcularPercentualFrequencia(aluno, codigoDisciplina, periodo);
        freqPercent = desemp.getPercentualFrequencia();
      } catch (ValidacaoException ex) {
        freqPercent = 0.0;
      }
      media = 0.0;
      status = situacaoService.avaliarStatus(media, freqPercent);
    }

    return new Historico(
        aluno, periodo, codigoDisciplina, matriculaProfessor, media, freqPercent, status);
  }

  private Historico consolidarDiarios(
      String aluno, String codigoDisciplina, String periodo, List<Diario> diarios) {
    List<Double> medias = new ArrayList<>();
    Set<String> professores = new java.util.LinkedHashSet<>();

    for (Diario diario : diarios) {
      professores.add(diario.getMatriculaProfessor());
      Nota nota = notaRepository.buscarPorAlunoEDiario(aluno, diario.getCodigo());
      if (nota == null && diarios.size() == 1) {
        nota = notaRepository.buscarPorAlunoEDisciplina(aluno, codigoDisciplina, periodo);
      }
      if (nota != null) {
        medias.add(calcularMediaDoDiario(nota, diario));
      }
    }

    double mediaFinal = medias.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    double frequenciaFinal = calcularFrequenciaAgregada(aluno, diarios);
    StatusAcademico status = situacaoService.avaliarStatus(mediaFinal, frequenciaFinal);

    return new Historico(
        aluno,
        periodo,
        codigoDisciplina,
        String.join(",", professores),
        mediaFinal,
        frequenciaFinal,
        status);
  }

  private double calcularMediaDoDiario(Nota nota, Diario diario) {
    List<Avaliacao> avaliacoes = avaliacaoRepository.buscarPorDiario(diario.getCodigo());
    if (!avaliacoes.isEmpty()) {
      double soma = 0.0;
      double pesos = 0.0;
      for (Avaliacao avaliacao : avaliacoes) {
        double valor = obterNotaDaEtapa(nota, avaliacao.getEtapa());
        if (valor >= 0.0) {
          soma += (valor / avaliacao.getNotaMaxima()) * 10.0 * avaliacao.getPeso();
          pesos += avaliacao.getPeso();
        }
      }
      if (pesos > 0.0) {
        return soma / pesos;
      }
    }

    double soma = 0.0;
    int quantidade = 0;
    for (double valor : new double[] {nota.getNota1(), nota.getNota2(), nota.getNota3()}) {
      if (valor >= 0.0) {
        soma += valor;
        quantidade++;
      }
    }
    return quantidade == 0 ? 0.0 : soma / quantidade;
  }

  private double obterNotaDaEtapa(Nota nota, int etapa) {
    if (etapa == 1) return nota.getNota1();
    if (etapa == 2) return nota.getNota2();
    if (etapa == 3) return nota.getNota3();
    return -1.0;
  }

  private double calcularFrequenciaAgregada(String aluno, List<Diario> diarios) {
    Set<String> codigosDiario = new HashSet<>();
    for (Diario diario : diarios) {
      codigosDiario.add(diario.getCodigo().toUpperCase(Locale.ROOT));
    }

    int total = 0;
    int presencas = 0;
    for (Frequencia frequencia : frequenciaRepository.buscarTodas()) {
      if (frequencia.getMatriculaAluno().equalsIgnoreCase(aluno)
          && codigosDiario.contains(frequencia.getCodigoDiario().toUpperCase(Locale.ROOT))) {
        total++;
        if (frequencia.getStatus() == Frequencia.TipoFrequencia.PRESENCA) {
          presencas++;
        }
      }
    }
    return total == 0 ? 100.0 : (presencas * 100.0) / total;
  }

  private List<Diario> buscarDiariosFechados(String codigoDisciplina, String periodo) {
    List<Diario> diarios = new ArrayList<>();
    for (Diario diario : diarioRepository.buscarTodos()) {
      if (diario.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && diario.getPeriodo().equalsIgnoreCase(periodo)
          && diario.isFechado()) {
        diarios.add(diario);
      }
    }
    return diarios;
  }

  private Set<String> carregarChavesConsolidadas(String periodo) {
    Set<String> chaves = new HashSet<>();
    for (Historico historico : historicoRepository.buscarTodos()) {
      if (historico.getPeriodo().equalsIgnoreCase(periodo)) {
        chaves.add(
            montarChaveHistorico(
                historico.getMatriculaAluno(), historico.getCodigoDisciplina(), periodo));
      }
    }
    return chaves;
  }

  private String montarChaveHistorico(
      String matriculaAluno, String codigoDisciplina, String periodo) {
    return (matriculaAluno + ";" + codigoDisciplina + ";" + periodo).toUpperCase(Locale.ROOT);
  }

  private int compararPeriodos(String primeiroPeriodo, String segundoPeriodo) {
    String[] primeiro = primeiroPeriodo.split("\\.");
    String[] segundo = segundoPeriodo.split("\\.");

    if (primeiro.length == 2
        && segundo.length == 2
        && primeiro[0].matches("\\d+")
        && segundo[0].matches("\\d+")
        && primeiro[1].matches("\\d+")
        && segundo[1].matches("\\d+")) {
      int ano = Integer.compare(Integer.parseInt(primeiro[0]), Integer.parseInt(segundo[0]));
      if (ano != 0) {
        return ano;
      }
      return Integer.compare(Integer.parseInt(primeiro[1]), Integer.parseInt(segundo[1]));
    }

    return primeiroPeriodo.compareToIgnoreCase(segundoPeriodo);
  }
}
