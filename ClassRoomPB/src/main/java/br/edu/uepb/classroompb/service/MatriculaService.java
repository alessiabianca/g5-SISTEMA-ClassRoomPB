package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Disciplina;
import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.StatusAcademico;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioAlunoException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.IOException;
import java.util.List;

public class MatriculaService {
  private final TurmaRepository turmaRepository;
  private final MatriculaRepository matriculaRepository;
  private final PeriodoRepository periodoRepository;
  private final DisciplinaRepository disciplinaRepository;
  private final HistoricoRepository historicoRepository;

  public MatriculaService(
      TurmaRepository turmaRepository,
      MatriculaRepository matriculaRepository,
      PeriodoRepository periodoRepository,
      DisciplinaRepository disciplinaRepository,
      HistoricoRepository historicoRepository) {
    this.turmaRepository = turmaRepository;
    this.matriculaRepository = matriculaRepository;
    this.periodoRepository = periodoRepository;
    this.disciplinaRepository = disciplinaRepository;
    this.historicoRepository = historicoRepository;
  }

  /**
   * Solicita a matrícula de um estudante. [TASK 2280 / TASK 2273] Lógica de Enfileiramento: se a
   * turma estiver lotada, o sistema não rejeita a requisição, mas adiciona à lista de espera e
   * salva o estado.
   */
  public Matricula solicitarMatricula(
      String matriculaAluno, String codigoDisciplina, String periodo)
      throws ChoqueHorarioAlunoException, ValidacaoException {

    validarPreRequisitos(matriculaAluno, codigoDisciplina);

    Turma turma = buscarTurmaNoRepositorio(codigoDisciplina, periodo);
    if (turma == null) {
      throw new ValidacaoException(
          "Turma não encontrada para esta disciplina no período informado.");
    }

    List<Matricula> matriculasAtuais = matriculaRepository.buscarTodas();

    validarChoqueHorarioAluno(matriculaAluno, codigoDisciplina, periodo, matriculasAtuais);

    Matricula.StatusMatricula statusFinal = Matricula.StatusMatricula.CONFIRMADA;

    if (turma.getVagasOcupadas() >= turma.getVagas()) {

      statusFinal = Matricula.StatusMatricula.ESPERA;
      turma.getListaEsperaMatriculas().add(matriculaAluno);
    } else {

      turma.setVagasOcupadas(turma.getVagasOcupadas() + 1);
    }

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
          "Ação Bloqueada: Cancelamento não permitido. O período letivo '"
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

    if (statusRemovido == Matricula.StatusMatricula.CONFIRMADA
        || statusRemovido == Matricula.StatusMatricula.SOLICITADA) {
      boolean promoveuAlguem = false;

      for (Matricula m : matriculasAtuais) {
        if (m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
            && m.getPeriodo().equalsIgnoreCase(periodo)
            && m.getStatus() == Matricula.StatusMatricula.ESPERA) {

          m.transitarPara(Matricula.StatusMatricula.CONFIRMADA);
          promoveuAlguem = true;
          break;
        }
      }

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
    // A verificação de choque de horário passa a ser gerenciada no Diário (Release 4)
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

  private void validarPreRequisitos(String matriculaAluno, String codigoDisciplina)
      throws ValidacaoException {
    try {
      Disciplina disciplina = disciplinaRepository.buscarPorCodigo(codigoDisciplina);
      if (disciplina == null) {
        throw new ValidacaoException("Disciplina não encontrada: " + codigoDisciplina);
      }
      List<String> preReqs = disciplina.getPreRequisitosCodigos();
      if (preReqs != null && !preReqs.isEmpty()) {
        List<Historico> historicos = historicoRepository.buscarPorAluno(matriculaAluno);
        for (String req : preReqs) {
          boolean aprovado = false;
          for (Historico h : historicos) {
            if (h.getCodigoDisciplina().equalsIgnoreCase(req)
                && h.getStatus() == StatusAcademico.APROVADO) {
              aprovado = true;
              break;
            }
          }
          if (!aprovado) {
            throw new ValidacaoException("Pré-requisito não cumprido: " + req);
          }
        }
      }
    } catch (IOException e) {
      throw new ValidacaoException("Erro ao acessar repositório de disciplinas: " + e.getMessage());
    }
  }
}
