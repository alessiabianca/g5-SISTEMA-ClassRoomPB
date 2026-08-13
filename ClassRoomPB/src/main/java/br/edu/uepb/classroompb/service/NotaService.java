package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Avaliacao;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.repository.AvaliacaoRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.List;

public class NotaService {
  private final NotaRepository notaRepository;
  private final TurmaRepository turmaRepository;
  private final MatriculaRepository matriculaRepository;
  private final PeriodoRepository periodoRepository;
  private final AvaliacaoRepository avaliacaoRepository;
  private final DiarioRepository diarioRepository;

  public NotaService(
      NotaRepository notaRepository,
      TurmaRepository turmaRepository,
      MatriculaRepository matriculaRepository,
      PeriodoRepository periodoRepository,
      AvaliacaoRepository avaliacaoRepository,
      DiarioRepository diarioRepository) {
    this.notaRepository = notaRepository;
    this.turmaRepository = turmaRepository;
    this.matriculaRepository = matriculaRepository;
    this.periodoRepository = periodoRepository;
    this.avaliacaoRepository = avaliacaoRepository;
    this.diarioRepository = diarioRepository;
  }

  /**
   * [TASK 2523] LÃ³gica de Busca de Notas no ServiÃ§o. Varre e filtra as notas registradas no
   * sistema associadas exclusivamente ao aluno e ao perÃ­odo ativo. Caso o aluno esteja matriculado
   * em uma disciplina que ainda nÃ£o teve notas lanÃ§adas, retorna uma estrutura padrÃ£o (zerada)
   * para compor o painel visual sem falhas de carregamento.
   */
  public List<Nota> buscarNotasPorAlunoEPeriodo(String matriculaAluno, String periodo)
      throws ValidacaoException {
    if (matriculaAluno == null || matriculaAluno.trim().isEmpty()) {
      throw new ValidacaoException("Erro de Busca: A matrÃ­cula do estudante Ã© invÃ¡lida.");
    }

    List<Nota> notasDoPeriodo = new ArrayList<>();
    List<Matricula> todasMatriculas = matriculaRepository.buscarTodas();
    List<Nota> todasNotas = notaRepository.buscarTodas();

    for (Matricula m : todasMatriculas) {
      if (m.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && m.getPeriodo().equalsIgnoreCase(periodo)
          && m.getStatus() == Matricula.StatusMatricula.CONFIRMADA) {

        Nota notaEncontrada = null;
        for (Nota n : todasNotas) {
          if (n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
              && n.getCodigoDisciplina().equalsIgnoreCase(m.getCodigoDisciplina())
              && n.getPeriodo().equalsIgnoreCase(periodo)) {
            notaEncontrada = n;
            break;
          }
        }

        if (notaEncontrada != null) {
          notasDoPeriodo.add(notaEncontrada);
        } else {
          notasDoPeriodo.add(
              new Nota(matriculaAluno, m.getCodigoDisciplina(), periodo, 0.0, 0.0, -1.0));
        }
      }
    }

    return notasDoPeriodo;
  }

  public void lancarNota(
      String matriculaProfessor, String matriculaAluno, String idAvaliacao, double valorNota)
      throws ValidacaoException {

    AvaliacaoEDiario ctx = buscarEValidarAvaliacao(idAvaliacao, matriculaProfessor);

    if (valorNota < 0.0 || valorNota > ctx.avaliacao.getNotaMaxima()) {
      throw new ValidacaoException(
          "Erro: Nota invÃ¡lida. O valor informado deve estar no intervalo de 0.0 a "
              + ctx.avaliacao.getNotaMaxima()
              + ".");
    }

    validarAlunoConfirmadoNoDiario(matriculaAluno, ctx.diario);

    List<Nota> todasNotas = notaRepository.buscarTodas();
    Nota notaExistente = null;

    for (Nota n : todasNotas) {
      if (n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && n.getCodigoDisciplina().equalsIgnoreCase(ctx.diario.getCodigoDisciplina())
          && n.getPeriodo().equalsIgnoreCase(ctx.diario.getPeriodo())
          && pertenceAoDiario(n, ctx.diario.getCodigo())) {
        notaExistente = n;
        break;
      }
    }

    if (notaExistente != null) {
      if (ctx.avaliacao.getEtapa() == 1) {
        notaExistente.setNota1(valorNota);
      } else if (ctx.avaliacao.getEtapa() == 2) {
        notaExistente.setNota2(valorNota);
      } else if (ctx.avaliacao.getEtapa() == 3) {
        notaExistente.setNota3(valorNota);
      }
    } else {
      double n1 = (ctx.avaliacao.getEtapa() == 1) ? valorNota : -1.0;
      double n2 = (ctx.avaliacao.getEtapa() == 2) ? valorNota : -1.0;
      double n3 = (ctx.avaliacao.getEtapa() == 3) ? valorNota : -1.0;
      notaExistente =
          new Nota(
              matriculaAluno,
              ctx.diario.getCodigoDisciplina(),
              ctx.diario.getPeriodo(),
              ctx.diario.getCodigo(),
              n1,
              n2,
              n3);
      todasNotas.add(notaExistente);
    }

    atualizarArquivoCompletoLocal(todasNotas);
  }

  public void retificarNota(
      String matriculaProfessor, String matriculaAluno, String idAvaliacao, double novoValor)
      throws ValidacaoException {

    AvaliacaoEDiario ctx = buscarEValidarAvaliacao(idAvaliacao, matriculaProfessor);

    Periodo periodoLetivo = periodoRepository.buscarPorCodigo(ctx.diario.getPeriodo());
    if (periodoLetivo != null && "ENCERRADO".equalsIgnoreCase(periodoLetivo.getStatus())) {
      throw new ValidacaoException(
          "Erro: O perÃ­odo letivo '"
              + ctx.diario.getPeriodo()
              + "' estÃ¡ ENCERRADO. NÃ£o Ã© permitido retificar notas.");
    }

    if (novoValor < 0.0 || novoValor > ctx.avaliacao.getNotaMaxima()) {
      throw new ValidacaoException(
          "Erro: Nota invÃ¡lida. O valor informado deve estar no intervalo de 0.0 a "
              + ctx.avaliacao.getNotaMaxima()
              + ".");
    }

    validarAlunoConfirmadoNoDiario(matriculaAluno, ctx.diario);

    List<Nota> todasNotas = notaRepository.buscarTodas();
    Nota notaExistente = null;

    for (Nota n : todasNotas) {
      if (n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && n.getCodigoDisciplina().equalsIgnoreCase(ctx.diario.getCodigoDisciplina())
          && n.getPeriodo().equalsIgnoreCase(ctx.diario.getPeriodo())
          && pertenceAoDiario(n, ctx.diario.getCodigo())) {
        notaExistente = n;
        break;
      }
    }

    if (notaExistente == null) {
      throw new ValidacaoException(
          "Erro: NÃ£o hÃ¡ nota lanÃ§ada para este aluno nesta disciplina/perÃ­odo. Utilize o comando 'lancarNota' primeiro.");
    }

    if (ctx.avaliacao.getEtapa() == 1) {
      notaExistente.setNota1(novoValor);
    } else if (ctx.avaliacao.getEtapa() == 2) {
      notaExistente.setNota2(novoValor);
    } else if (ctx.avaliacao.getEtapa() == 3) {
      notaExistente.setNota3(novoValor);
    }

    atualizarArquivoCompletoLocal(todasNotas);
  }

  /**
   * Busca e valida uma avaliaÃ§Ã£o e seu diÃ¡rio associado. Garante que: (1) a avaliaÃ§Ã£o existe,
   * (2) o diÃ¡rio existe, (3) o professor logado Ã© o responsÃ¡vel pelo diÃ¡rio, (4) o diÃ¡rio nÃ£o
   * estÃ¡ fechado. Elimina a duplicaÃ§Ã£o de lÃ³gica entre lancarNota() e retificarNota().
   *
   * @throws ValidacaoException em qualquer falha de validaÃ§Ã£o
   */
  private AvaliacaoEDiario buscarEValidarAvaliacao(String idAvaliacao, String matriculaProfessor)
      throws ValidacaoException {

    Avaliacao avaliacao = null;
    for (Avaliacao av : avaliacaoRepository.buscarTodas()) {
      if (av.getId().equalsIgnoreCase(idAvaliacao)) {
        avaliacao = av;
        break;
      }
    }

    if (avaliacao == null) {
      throw new ValidacaoException("AvaliaÃ§Ã£o '" + idAvaliacao + "' nÃ£o encontrada.");
    }

    Diario diario = diarioRepository.buscarPorCodigo(avaliacao.getCodigoDiario());

    if (diario == null) {
      throw new ValidacaoException("DiÃ¡rio da avaliaÃ§Ã£o nÃ£o encontrado.");
    }

    if (!diario.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
      throw new ValidacaoException(
          "AÃ§Ã£o nÃ£o permitida: VocÃª nÃ£o Ã© o professor responsÃ¡vel por este diÃ¡rio.");
    }

    if (diario.isFechado()) {
      throw new ValidacaoException(
          "O DiÃ¡rio de Classe jÃ¡ estÃ¡ encerrado e nÃ£o permite ediÃ§Ãµes.");
    }

    return new AvaliacaoEDiario(avaliacao, diario);
  }

  private void validarAlunoConfirmadoNoDiario(String matriculaAluno, Diario diario)
      throws ValidacaoException {
    if (matriculaAluno == null || matriculaAluno.trim().isEmpty()) {
      throw new ValidacaoException("Erro: A matricula do aluno e obrigatoria para lancar nota.");
    }

    for (Matricula matricula : matriculaRepository.buscarTodas()) {
      if (matricula.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && matricula.getCodigoDisciplina().equalsIgnoreCase(diario.getCodigoDisciplina())
          && matricula.getPeriodo().equalsIgnoreCase(diario.getPeriodo())
          && matricula.getStatus() == Matricula.StatusMatricula.CONFIRMADA) {
        return;
      }
    }

    throw new ValidacaoException(
        "Erro: Aluno '"
            + matriculaAluno
            + "' nao possui matricula CONFIRMADA na turma deste diario.");
  }

  /** Estrutura interna para retornar avaliaÃ§Ã£o e diÃ¡rio juntos do mÃ©todo de validaÃ§Ã£o. */
  private static class AvaliacaoEDiario {
    final Avaliacao avaliacao;
    final Diario diario;

    AvaliacaoEDiario(Avaliacao avaliacao, Diario diario) {
      this.avaliacao = avaliacao;
      this.diario = diario;
    }
  }

  private void atualizarArquivoCompletoLocal(List<Nota> notasAtualizadas) {
    String FILE_PATH = "data/notas.txt";
    try (java.io.BufferedWriter bw =
        new java.io.BufferedWriter(new java.io.FileWriter(FILE_PATH, false))) {
      for (Nota n : notasAtualizadas) {
        bw.write(n.toString());
        bw.newLine();
      }
    } catch (java.io.IOException e) {
      System.err.println("Erro ao reescrever o arquivo de notas: " + e.getMessage());
    }
  }

  private boolean pertenceAoDiario(Nota nota, String codigoDiario) {
    return nota.getCodigoDiario() == null
        || nota.getCodigoDiario().isBlank()
        || nota.getCodigoDiario().equalsIgnoreCase(codigoDiario);
  }
}
