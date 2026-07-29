package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.OcupacaoVagasTurma;
import br.edu.uepb.classroompb.model.RelatorioOcupacaoVagas;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.List;

public class RelatorioOcupacaoVagasService {
  private final TurmaRepository turmaRepository;
  private final MatriculaRepository matriculaRepository;

  public RelatorioOcupacaoVagasService(
      TurmaRepository turmaRepository, MatriculaRepository matriculaRepository) {
    if (turmaRepository == null || matriculaRepository == null) {
      throw new IllegalArgumentException("Repositorios de turmas e matriculas sao obrigatorios.");
    }
    this.turmaRepository = turmaRepository;
    this.matriculaRepository = matriculaRepository;
  }

  public RelatorioOcupacaoVagas gerarRelatorioOcupacaoVagas() {
    return montarRelatorio(turmaRepository.buscarTodas());
  }

  public RelatorioOcupacaoVagas gerarRelatorioOcupacaoVagasPorPeriodo(String codigoPeriodo)
      throws ValidacaoException {
    validarCampoObrigatorio(codigoPeriodo, "periodo");

    List<Turma> turmasDoPeriodo = new ArrayList<>();
    for (Turma turma : turmaRepository.buscarTodas()) {
      if (equalsIgnoreCase(turma.getPeriodo(), codigoPeriodo.trim())) {
        turmasDoPeriodo.add(turma);
      }
    }

    return montarRelatorio(turmasDoPeriodo);
  }

  public OcupacaoVagasTurma calcularOcupacaoVagasTurma(
      String codigoDisciplina, String codigoPeriodo) throws ValidacaoException {
    validarCampoObrigatorio(codigoDisciplina, "disciplina");
    validarCampoObrigatorio(codigoPeriodo, "periodo");

    Turma turmaAlvo = null;
    for (Turma turma : turmaRepository.buscarTodas()) {
      if (equalsIgnoreCase(turma.getCodigoDisciplina(), codigoDisciplina.trim())
          && equalsIgnoreCase(turma.getPeriodo(), codigoPeriodo.trim())) {
        turmaAlvo = turma;
        break;
      }
    }

    if (turmaAlvo == null) {
      throw new ValidacaoException("Erro: A turma informada nao existe no sistema.");
    }

    return calcularOcupacao(turmaAlvo, matriculaRepository.buscarTodas());
  }

  private RelatorioOcupacaoVagas montarRelatorio(List<Turma> turmas) {
    List<Matricula> matriculas = matriculaRepository.buscarTodas();
    List<OcupacaoVagasTurma> ocupacoes = new ArrayList<>();

    if (turmas != null) {
      for (Turma turma : turmas) {
        ocupacoes.add(calcularOcupacao(turma, matriculas));
      }
    }

    return new RelatorioOcupacaoVagas(ocupacoes);
  }

  private OcupacaoVagasTurma calcularOcupacao(Turma turma, List<Matricula> matriculas) {
    int vagasOcupadas = 0;
    int alunosEmEspera = 0;

    if (matriculas != null) {
      for (Matricula matricula : matriculas) {
        if (!isDaTurma(matricula, turma)) {
          continue;
        }

        if (ocupaVaga(matricula.getStatus())) {
          vagasOcupadas++;
        } else if (matricula.getStatus() == Matricula.StatusMatricula.ESPERA) {
          alunosEmEspera++;
        }
      }
    }

    return new OcupacaoVagasTurma(
        turma.getCodigoDisciplina(),
        turma.getPeriodo(),
        "N/A", // Professor migrado para o Diario
        "N/A", // Horario migrado para o Diario
        "N/A", // Sala migrada para o Diario
        turma.getVagas(),
        vagasOcupadas,
        alunosEmEspera);
  }

  private boolean isDaTurma(Matricula matricula, Turma turma) {
    return matricula != null
        && turma != null
        && equalsIgnoreCase(matricula.getCodigoDisciplina(), turma.getCodigoDisciplina())
        && equalsIgnoreCase(matricula.getPeriodo(), turma.getPeriodo());
  }

  private boolean ocupaVaga(Matricula.StatusMatricula status) {
    return status == Matricula.StatusMatricula.CONFIRMADA
        || status == Matricula.StatusMatricula.SOLICITADA;
  }

  private void validarCampoObrigatorio(String valor, String campo) throws ValidacaoException {
    if (valor == null || valor.trim().isEmpty()) {
      throw new ValidacaoException("Erro: O codigo da " + campo + " deve ser informado.");
    }
  }

  private boolean equalsIgnoreCase(String primeiro, String segundo) {
    return primeiro != null && segundo != null && primeiro.equalsIgnoreCase(segundo);
  }
}