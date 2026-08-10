package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
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

  public NotaService(
      NotaRepository notaRepository,
      TurmaRepository turmaRepository,
      MatriculaRepository matriculaRepository,
      PeriodoRepository periodoRepository) {
    this.notaRepository = notaRepository;
    this.turmaRepository = turmaRepository;
    this.matriculaRepository = matriculaRepository;
    this.periodoRepository = periodoRepository;
  }

  /**
   * [TASK 2523] Lógica de Busca de Notas no Serviço. Varre e filtra as notas registradas no sistema
   * associadas exclusivamente ao aluno e ao período ativo. Caso o aluno esteja matriculado em uma
   * disciplina que ainda não teve notas lançadas, retorna uma estrutura padrão (zerada) para compor
   * o painel visual sem falhas de carregamento.
   */
  public List<Nota> buscarNotasPorAlunoEPeriodo(String matriculaAluno, String periodo)
      throws ValidacaoException {
    if (matriculaAluno == null || matriculaAluno.trim().isEmpty()) {
      throw new ValidacaoException("Erro de Busca: A matrícula do estudante é inválida.");
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

    br.edu.uepb.classroompb.repository.AvaliacaoRepository avaliacaoRepo =
        new br.edu.uepb.classroompb.repository.AvaliacaoRepository();
    br.edu.uepb.classroompb.model.Avaliacao avaliacao = null;

    for (br.edu.uepb.classroompb.model.Avaliacao av : avaliacaoRepo.buscarTodas()) {
      if (av.getId().equalsIgnoreCase(idAvaliacao)) {
        avaliacao = av;
        break;
      }
    }

    if (avaliacao == null) {
      throw new ValidacaoException("Avaliação '" + idAvaliacao + "' não encontrada.");
    }

    br.edu.uepb.classroompb.repository.DiarioRepository diarioRepo =
        new br.edu.uepb.classroompb.repository.DiarioRepository();
    br.edu.uepb.classroompb.model.Diario diario =
        diarioRepo.buscarPorCodigo(avaliacao.getCodigoDiario());

    if (diario == null) {
      throw new ValidacaoException("Diário da avaliação não encontrado.");
    }

    if (!diario.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
      throw new ValidacaoException(
          "Ação não permitida: Você não é o professor responsável por este diário.");
    }

    if (diario.isFechado()) {
      throw new ValidacaoException("O Diário de Classe já está encerrado e não permite edições.");
    }

    if (valorNota < 0.0 || valorNota > avaliacao.getNotaMaxima()) {
      throw new ValidacaoException(
          "Erro: Nota inválida. O valor informado deve estar no intervalo de 0.0 a "
              + avaliacao.getNotaMaxima()
              + ".");
    }

    List<Nota> todasNotas = notaRepository.buscarTodas();
    Nota notaExistente = null;

    for (Nota n : todasNotas) {
      if (n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && n.getCodigoDisciplina().equalsIgnoreCase(diario.getCodigoDisciplina())
          && n.getPeriodo().equalsIgnoreCase(diario.getPeriodo())) {
        notaExistente = n;
        break;
      }
    }

    if (notaExistente != null) {
      if (avaliacao.getEtapa() == 1) {
        notaExistente.setNota1(valorNota);
      } else if (avaliacao.getEtapa() == 2) {
        notaExistente.setNota2(valorNota);
      } else if (avaliacao.getEtapa() == 3) {
        notaExistente.setNota3(valorNota);
      }
    } else {
      double n1 = (avaliacao.getEtapa() == 1) ? valorNota : 0.0;
      double n2 = (avaliacao.getEtapa() == 2) ? valorNota : 0.0;
      double n3 = (avaliacao.getEtapa() == 3) ? valorNota : -1.0;
      notaExistente =
          new Nota(matriculaAluno, diario.getCodigoDisciplina(), diario.getPeriodo(), n1, n2, n3);
      todasNotas.add(notaExistente);
    }

    atualizarArquivoCompletoLocal(todasNotas);
  }

  public void retificarNota(
      String matriculaProfessor, String matriculaAluno, String idAvaliacao, double novoValor)
      throws ValidacaoException {

    br.edu.uepb.classroompb.repository.AvaliacaoRepository avaliacaoRepo =
        new br.edu.uepb.classroompb.repository.AvaliacaoRepository();
    br.edu.uepb.classroompb.model.Avaliacao avaliacao = null;

    for (br.edu.uepb.classroompb.model.Avaliacao av : avaliacaoRepo.buscarTodas()) {
      if (av.getId().equalsIgnoreCase(idAvaliacao)) {
        avaliacao = av;
        break;
      }
    }

    if (avaliacao == null) {
      throw new ValidacaoException("Avaliação '" + idAvaliacao + "' não encontrada.");
    }

    br.edu.uepb.classroompb.repository.DiarioRepository diarioRepo =
        new br.edu.uepb.classroompb.repository.DiarioRepository();
    br.edu.uepb.classroompb.model.Diario diario =
        diarioRepo.buscarPorCodigo(avaliacao.getCodigoDiario());

    if (diario == null) {
      throw new ValidacaoException("Diário da avaliação não encontrado.");
    }

    if (!diario.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
      throw new ValidacaoException(
          "Ação não permitida: Você não é o professor responsável por este diário.");
    }

    if (diario.isFechado()) {
      throw new ValidacaoException("O Diário de Classe já está encerrado e não permite edições.");
    }

    Periodo periodoLetivo = periodoRepository.buscarPorCodigo(diario.getPeriodo());
    if (periodoLetivo != null && "ENCERRADO".equalsIgnoreCase(periodoLetivo.getStatus())) {
      throw new ValidacaoException(
          "Erro: O período letivo '"
              + diario.getPeriodo()
              + "' está ENCERRADO. Não é permitido retificar notas.");
    }

    if (novoValor < 0.0 || novoValor > avaliacao.getNotaMaxima()) {
      throw new ValidacaoException(
          "Erro: Nota inválida. O valor informado deve estar no intervalo de 0.0 a "
              + avaliacao.getNotaMaxima()
              + ".");
    }

    List<Nota> todasNotas = notaRepository.buscarTodas();
    Nota notaExistente = null;

    for (Nota n : todasNotas) {
      if (n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && n.getCodigoDisciplina().equalsIgnoreCase(diario.getCodigoDisciplina())
          && n.getPeriodo().equalsIgnoreCase(diario.getPeriodo())) {
        notaExistente = n;
        break;
      }
    }

    if (notaExistente == null) {
      throw new ValidacaoException(
          "Erro: Não há nota lançada para este aluno nesta disciplina/período. Utilize o comando 'lancarNota' primeiro.");
    }

    if (avaliacao.getEtapa() == 1) {
      notaExistente.setNota1(novoValor);
    } else if (avaliacao.getEtapa() == 2) {
      notaExistente.setNota2(novoValor);
    } else if (avaliacao.getEtapa() == 3) {
      notaExistente.setNota3(novoValor);
    }

    atualizarArquivoCompletoLocal(todasNotas);
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
}
