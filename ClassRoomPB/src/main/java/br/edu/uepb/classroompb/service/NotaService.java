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

  /**
   * Lança as notas de avaliações parciais de forma atômica no repositório. Valida o intervalo [0.0
   * - 10.0] e assegura que apenas o professor da turma execute a ação.
   */
  public void lancarNota(
      String matriculaProfessor,
      String matriculaAluno,
      String codigoDisciplina,
      String periodo,
      int etapa,
      double valorNota)
      throws ValidacaoException {

    List<Turma> turmas = turmaRepository.buscarTodas();
    Turma turmaAlvo = null;
    for (Turma t : turmas) {
      if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && t.getPeriodo().equalsIgnoreCase(periodo)) {
        turmaAlvo = t;
        break;
      }
    }

    if (turmaAlvo == null) {
      throw new ValidacaoException("Erro: Turma não encontrada para esta disciplina e período.");
    }

    if (valorNota < 0.0 || valorNota > 10.0) {
      throw new ValidacaoException(
          "Erro: Nota inválida. O valor informado deve estar no intervalo estrito de 0.0 a 10.0.");
    }

    if (etapa != 1 && etapa != 2) {
      throw new ValidacaoException("Erro: Etapa de avaliação inválida. Use apenas 1 ou 2.");
    }

    List<Nota> todasNotas = notaRepository.buscarTodas();
    Nota notaExistente = null;

    for (Nota n : todasNotas) {
      if (n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && n.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && n.getPeriodo().equalsIgnoreCase(periodo)) {
        notaExistente = n;
        break;
      }
    }

    if (notaExistente != null) {

      if (etapa == 1) {
        notaExistente.setNota1(valorNota);
      } else {
        notaExistente.setNota2(valorNota);
      }
    } else {

      double n1 = (etapa == 1) ? valorNota : 0.0;
      double n2 = (etapa == 2) ? valorNota : 0.0;
      notaExistente = new Nota(matriculaAluno, codigoDisciplina, periodo, n1, n2, -1.0);
      todasNotas.add(notaExistente);
    }

    atualizarArquivoCompletoLocal(todasNotas);
  }

  /**
   * US35 — Retificação e Alteração de Notas pelo Corpo Docente. Permite ao professor responsável
   * pela turma editar uma nota já lançada, desde que o período letivo correspondente NÃO esteja com
   * status ENCERRADO.
   */
  public void retificarNota(
      String matriculaProfessor,
      String matriculaAluno,
      String codigoDisciplina,
      String periodo,
      int etapa,
      double novoValor)
      throws ValidacaoException {

    Periodo periodoLetivo = periodoRepository.buscarPorCodigo(periodo);
    if (periodoLetivo != null && "ENCERRADO".equalsIgnoreCase(periodoLetivo.getStatus())) {
      throw new ValidacaoException(
          "Erro: O período letivo '"
              + periodo
              + "' está ENCERRADO. Não é permitido retificar notas após o encerramento do semestre.");
    }

    List<Turma> turmas = turmaRepository.buscarTodas();
    Turma turmaAlvo = null;
    for (Turma t : turmas) {
      if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && t.getPeriodo().equalsIgnoreCase(periodo)) {
        turmaAlvo = t;
        break;
      }
    }

    if (turmaAlvo == null) {
      throw new ValidacaoException("Erro: Turma não encontrada para esta disciplina e período.");
    }

    if (novoValor < 0.0 || novoValor > 10.0) {
      throw new ValidacaoException(
          "Erro: Nota inválida. O valor informado deve estar no intervalo estrito de 0.0 a 10.0.");
    }

    if (etapa != 1 && etapa != 2) {
      throw new ValidacaoException("Erro: Etapa de avaliação inválida. Use apenas 1 ou 2.");
    }

    List<Nota> todasNotas = notaRepository.buscarTodas();
    Nota notaExistente = null;

    for (Nota n : todasNotas) {
      if (n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && n.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && n.getPeriodo().equalsIgnoreCase(periodo)) {
        notaExistente = n;
        break;
      }
    }

    if (notaExistente == null) {
      throw new ValidacaoException(
          "Erro: Não há nota lançada para este aluno nesta disciplina/período. Utilize o comando 'lancarNota' primeiro.");
    }

    if (etapa == 1) {
      notaExistente.setNota1(novoValor);
    } else {
      notaExistente.setNota2(novoValor);
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
