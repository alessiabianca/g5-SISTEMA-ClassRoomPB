package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.DesempenhoFrequencia;
import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HistoricoService {
  private final HistoricoRepository historicoRepository;
  private final MatriculaRepository matriculaRepository;
  private final TurmaRepository turmaRepository;
  private final SituacaoAcademicaService situacaoService;
  private final FrequenciaService frequenciaService;

  public HistoricoService(
      HistoricoRepository historicoRepository,
      MatriculaRepository matriculaRepository,
      TurmaRepository turmaRepository,
      SituacaoAcademicaService situacaoService,
      FrequenciaService frequenciaService) {
    this.historicoRepository = historicoRepository;
    this.matriculaRepository = matriculaRepository;
    this.turmaRepository = turmaRepository;
    this.situacaoService = situacaoService;
    this.frequenciaService = frequenciaService;
  }

  public void gerarHistoricoDoPeriodo(String periodo) {
    List<Matricula> matriculas = matriculaRepository.buscarTodas();
    List<Historico> historicosParaSalvar = new ArrayList<>();

    for (Matricula m : matriculas) {
      if (m.getPeriodo().equalsIgnoreCase(periodo)
          && m.getStatus() == Matricula.StatusMatricula.CONFIRMADA) {
        String aluno = m.getMatriculaAluno();
        String disc = m.getCodigoDisciplina();
        String matriculaProfessor = buscarMatriculaProfessor(disc, periodo);

        double media = 0.0;
        double freqPercent = 0.0;
        StatusAcademico status;

        try {
          SituacaoAcademicaService.ResultadoApuracao resultado =
              situacaoService.apurarSituacao(aluno, disc, periodo);
          media = resultado.getMedia();
          freqPercent = resultado.getDesempenho().getPercentualFrequencia();
          status = resultado.getStatus();
        } catch (ValidacaoException e) {
          // Trata o cenário onde a nota não foi lançada
          try {
            DesempenhoFrequencia desemp =
                frequenciaService.calcularPercentualFrequencia(aluno, disc, periodo);
            freqPercent = desemp.getPercentualFrequencia();
          } catch (ValidacaoException ex) {
            freqPercent = 0.0;
          }
          media = 0.0;
          status = situacaoService.avaliarStatus(media, freqPercent);
        }

        Historico h =
            new Historico(aluno, periodo, disc, matriculaProfessor, media, freqPercent, status);
        if (!historicoRepository.existe(aluno, disc, periodo)) {
          historicosParaSalvar.add(h);
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

  private String buscarMatriculaProfessor(String codigoDisciplina, String periodo) {
    for (Turma turma : turmaRepository.buscarTodas()) {
      if (turma.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && turma.getPeriodo().equalsIgnoreCase(periodo)) {
        return turma.getMatriculaProfessor();
      }
    }
    throw new IllegalStateException(
        "Turma nao encontrada para consolidar o historico da disciplina "
            + codigoDisciplina
            + " no periodo "
            + periodo
            + ".");
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
