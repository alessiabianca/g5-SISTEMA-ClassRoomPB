package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.DesempenhoFrequencia;
import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository; // Import do repositório de notas
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.List;

public class FrequenciaService {
  private final TurmaRepository turmaRepository;
  private final MatriculaRepository matriculaRepository;
  private final FrequenciaRepository frequenciaRepository;
  private final NotaRepository notaRepository; // Acoplamento do repositório para cálculo analítico

  // Construtor atualizado para receber o NotaRepository
  public FrequenciaService(
      TurmaRepository turmaRepository,
      MatriculaRepository matriculaRepository,
      FrequenciaRepository frequenciaRepository,
      NotaRepository notaRepository) {
    this.turmaRepository = turmaRepository;
    this.matriculaRepository = matriculaRepository;
    this.frequenciaRepository = frequenciaRepository;
    this.notaRepository = notaRepository;
  }

  public void registrarChamadaLote(
      String matriculaProfessor,
      String codigoDisciplina,
      String periodo,
      String dataAula,
      List<Matricula> alunosComStatus)
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
      throw new ValidacaoException(
          "Erro: Nenhuma turma ofertada para a disciplina '"
              + codigoDisciplina
              + "' no período '"
              + periodo
              + "'.");
    }

    if (!turmaAlvo.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
      throw new ValidacaoException(
          "Erro de Segurança: Você não possui permissão para lançar frequências na turma de outro docente.");
    }

    if (alunosComStatus == null || alunosComStatus.isEmpty()) {
      throw new ValidacaoException(
          "Erro: Nenhum registro de frequência foi enviado para processamento.");
    }

    List<Frequencia> loteParaSalvar = new ArrayList<>();
    for (Matricula m : alunosComStatus) {
      Frequencia.TipoFrequencia statusChamada;
      if (m.getStatus() == Matricula.StatusMatricula.SOLICITADA) {
        statusChamada = Frequencia.TipoFrequencia.FALTA;
      } else {
        statusChamada = Frequencia.TipoFrequencia.PRESENCA;
      }

      loteParaSalvar.add(
          new Frequencia(
              dataAula, m.getMatriculaAluno(), codigoDisciplina, periodo, statusChamada));
    }

    frequenciaRepository.salvarLote(loteParaSalvar);
  }

  /**
   * [TASK 2474] Processamento e Computação Automática de Média Final. Computa a média aritmética no
   * momento exato em que o extrato de assiduidade/desempenho é solicitado.
   */
  public DesempenhoFrequencia calcularPercentualFrequencia(
      String matriculaAluno, String codigoDisciplina, String periodo) throws ValidacaoException {

    boolean turmaExiste = false;
    for (Turma t : turmaRepository.buscarTodas()) {
      if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && t.getPeriodo().equalsIgnoreCase(periodo)) {
        turmaExiste = true;
        break;
      }
    }
    if (!turmaExiste) {
      throw new ValidacaoException(
          "Erro Analítico: A turma informada não existe no sistema corporativo para este período.");
    }

    // 1. Calcular frequência
    List<Frequencia> historico =
        frequenciaRepository.buscarPorAlunoEDisciplina(matriculaAluno, codigoDisciplina, periodo);

    int totalAulas = historico.size();
    int presencas = 0;
    int faltas = 0;

    for (Frequencia f : historico) {
      if (f.getStatus() == Frequencia.TipoFrequencia.PRESENCA) {
        presencas++;
      } else {
        faltas++;
      }
    }

    double percentual = 100.0;
    if (totalAulas > 0) {
      percentual = ((double) presencas / totalAulas) * 100.0;
    }

    // 2. Recuperar notas para computar a Média Final reativamente
    double n1 = 0.0;
    double n2 = 0.0;
    Nota notaAluno =
        notaRepository.buscarPorAlunoEDisciplina(matriculaAluno, codigoDisciplina, periodo);
    if (notaAluno != null) {
      n1 = notaAluno.getNota1();
      n2 = notaAluno.getNota2();
    }

    // Retorna o objeto DesempenhoFrequencia unificando o cálculo de notas e faltas
    return new DesempenhoFrequencia(totalAulas, presencas, faltas, percentual, n1, n2);
  }

  // ====================================================================
  // CÓDIGO EXCLUSIVO DA US30: REGRA DE NEGÓCIO RN08
  // ====================================================================
  public boolean verificarRiscoReprovacao(double percentualFrequencia) {
    return percentualFrequencia < 75.0;
  }
}
