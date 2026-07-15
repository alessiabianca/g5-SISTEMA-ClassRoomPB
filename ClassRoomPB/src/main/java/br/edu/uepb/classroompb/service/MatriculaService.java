package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioAlunoException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.List;

public class MatriculaService {
  private final TurmaRepository turmaRepository;
  private final MatriculaRepository matriculaRepository;
  private final PeriodoRepository periodoRepository;

  public MatriculaService(
      TurmaRepository turmaRepository,
      MatriculaRepository matriculaRepository,
      PeriodoRepository periodoRepository) {
    this.turmaRepository = turmaRepository;
    this.matriculaRepository = matriculaRepository;
    this.periodoRepository = periodoRepository;
  }

  /**
   * Solicita a matrícula de um estudante. [TASK 2280 / TASK 2273] Lógica de Enfileiramento: se a
   * turma estiver lotada, o sistema não rejeita a requisição, mas adiciona à lista de espera e
   * salva o estado.
   */
  public Matricula solicitarMatricula(
      String matriculaAluno, String codigoDisciplina, String periodo)
      throws ChoqueHorarioAlunoException, ValidacaoException {

    Turma turma = buscarTurmaNoRepositorio(codigoDisciplina, periodo);
    if (turma == null) {
      throw new ValidacaoException(
          "Turma não encontrada para esta disciplina no período informado.");
    }

    // Recupera a lista mantendo fidelmente a ordem cronológica de inserção obtida do arquivo plano
    List<Matricula> matriculasAtuais = matriculaRepository.buscarTodas();

    validarChoqueHorarioAluno(matriculaAluno, codigoDisciplina, periodo, matriculasAtuais);

    Matricula.StatusMatricula statusFinal = Matricula.StatusMatricula.SOLICITADA;

    // Verifica a disponibilidade pelo atributo de ocupação da turma
    if (turma.getVagasOcupadas() >= turma.getVagas()) {
      // [TASK 2273] Limite atingido: enviado de forma ordenada para a lista de espera
      statusFinal = Matricula.StatusMatricula.ESPERA;
      turma.getListaEsperaMatriculas().add(matriculaAluno);
    } else {
      // Se houver vaga, incrementa a ocupação oficial
      turma.setVagasOcupadas(turma.getVagasOcupadas() + 1);
    }

    // Salva o estado atualizado da turma (ocupação incrementada ou fila preenchida) no arquivo
    List<Turma> todasAsTurmas = turmaRepository.buscarTodas();
    for (int i = 0; i < todasAsTurmas.size(); i++) {
      Turma t = todasAsTurmas.get(i);
      if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && t.getPeriodo().equalsIgnoreCase(periodo)) {
        todasAsTurmas.set(i, turma);
        break;
      }
    }
    turmaRepository.atualizarArquivoCompleto(todasAsTurmas);

    Matricula novaMatricula = new Matricula(matriculaAluno, codigoDisciplina, periodo, statusFinal);

    // A persistência via append garante que o novo elemento se posicione estritamente no final
    // físico do arquivo (tail)
    matriculaRepository.salvar(novaMatricula);

    return novaMatricula;
  }

  /**
   * Cancelamento de Matrícula Ativa com Gatilho de Promoção Automática [TASK 2276] Se houver alunos
   * aguardando na fila de espera (FIFO), o primeiro da fila assume a vaga liberada automaticamente
   * com status CONFIRMADA.
   */
  public void cancelarMatricula(String matriculaAluno, String codigoDisciplina, String periodo)
      throws ValidacaoException {
    Periodo periodoLetivo = periodoRepository.buscarPorCodigo(periodo);

    if (periodoLetivo == null) {
      throw new ValidacaoException("Período letivo não encontrado.");
    }

    if (!periodoLetivo.isAbertoParaMatriculas()) {
      throw new ValidacaoException(
          "Ação Haeccoded Bloqueada: Cancelamento não permitido. O período letivo '"
              + periodo
              + "' não está aberto para modificações.");
    }

    List<Matricula> matriculasAtuais = matriculaRepository.buscarTodas();
    Matricula matriculaParaRemover = null;

    for (Matricula m : matriculasAtuais) {
      if (m.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && m.getPeriodo().equalsIgnoreCase(periodo)) {
        matriculaParaRemover = m;
        break;
      }
    }

    if (matriculaParaRemover == null) {
      throw new ValidacaoException("Matrícula não encontrada.");
    }

    Matricula.StatusMatricula statusRemovido = matriculaParaRemover.getStatus();
    matriculasAtuais.remove(matriculaParaRemover);

    // Se o cancelamento liberou uma vaga real, aciona o motor de convocação automática da fila
    if (statusRemovido == Matricula.StatusMatricula.CONFIRMADA
        || statusRemovido == Matricula.StatusMatricula.SOLICITADA) {
      boolean promoveuAlguem = false;

      // Varredura sequencial (FIFO) em busca do primeiro estudante com status ESPERA
      for (Matricula m : matriculasAtuais) {
        if (m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
            && m.getPeriodo().equalsIgnoreCase(periodo)
            && m.getStatus() == Matricula.StatusMatricula.ESPERA) {

          m.transitarPara(Matricula.StatusMatricula.CONFIRMADA);
          promoveuAlguem = true;
          break; // Promove apenas o primeiro da fila
        }
      }

      // Se ninguém estava na fila, decrementa o contador físico de ocupação na tabela de turmas
      if (!promoveuAlguem) {
        List<Turma> turmas = turmaRepository.buscarTodas();
        for (int i = 0; i < turmas.size(); i++) {
          Turma t = turmas.get(i);
          if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
              && t.getPeriodo().equalsIgnoreCase(periodo)) {
            if (t.getVagasOcupadas() > 0) {
              t.setVagasOcupadas(t.getVagasOcupadas() - 1);
              turmas.set(i, t);
            }
            break;
          }
        }
        turmaRepository.atualizarArquivoCompleto(turmas);
      }
    }

    matriculaRepository.atualizarArquivoCompleto(matriculasAtuais);
  }

  public void validarChoqueHorarioAluno(
      String matriculaAluno,
      String codigoNovaDisciplina,
      String periodo,
      List<Matricula> matriculasExistentes)
      throws ChoqueHorarioAlunoException, ValidacaoException {

    Turma novaTurma = buscarTurmaNoRepositorio(codigoNovaDisciplina, periodo);
    if (novaTurma == null) {
      throw new ValidacaoException(
          "Ação bloqueada: A turma para a disciplina '"
              + codigoNovaDisciplina
              + "' não está ofertada no período "
              + periodo
              + ".");
    }

    String horarioNovaTurma = novaTurma.getHorario();

    List<String> disciplinasDoAluno = new ArrayList<>();
    for (Matricula m : matriculasExistentes) {
      if (m.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && m.getPeriodo().equalsIgnoreCase(periodo)) {

        if (m.getStatus() == Matricula.StatusMatricula.CONFIRMADA
            || m.getStatus() == Matricula.StatusMatricula.SOLICITADA) {

          disciplinasDoAluno.add(m.getCodigoDisciplina());
        }
      }
    }

    List<Turma> todasAsTurmas = turmaRepository.buscarTodas();
    for (Turma turmaExistente : todasAsTurmas) {
      if (turmaExistente.getPeriodo().equalsIgnoreCase(periodo)) {
        if (disciplinasDoAluno.contains(turmaExistente.getCodigoDisciplina())) {
          if (turmaExistente.getHorario().equalsIgnoreCase(horarioNovaTurma)) {
            throw new ChoqueHorarioAlunoException(
                "Conflito de Grade: O aluno '"
                    + matriculaAluno
                    + "' já está matriculado na disciplina '"
                    + turmaExistente.getCodigoDisciplina()
                    + "' que ocorre no mesmo horário ("
                    + horarioNovaTurma
                    + ").");
          }
        }
      }
    }
  }

  private Turma buscarTurmaNoRepositorio(String codigoDisciplina, String periodo) {
    List<Turma> turmas = turmaRepository.buscarTodas();
    for (Turma t : turmas) {
      if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && t.getPeriodo().equalsIgnoreCase(periodo)) {
        return t;
      }
    }
    return null;
  }
}
