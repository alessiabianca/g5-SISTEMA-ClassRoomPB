package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Aula;
import br.edu.uepb.classroompb.model.DesempenhoFrequencia;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.AulaRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.List;

public class FrequenciaService {
  private final TurmaRepository turmaRepository;
  private final FrequenciaRepository frequenciaRepository;
  private final NotaRepository notaRepository;
  private final DiarioRepository diarioRepository;
  private final AulaRepository aulaRepository;

  public FrequenciaService(
      TurmaRepository turmaRepository,
      MatriculaRepository matriculaRepository,
      FrequenciaRepository frequenciaRepository,
      NotaRepository notaRepository,
      DiarioRepository diarioRepository,
      AulaRepository aulaRepository) {
    this.turmaRepository = turmaRepository;
    this.frequenciaRepository = frequenciaRepository;
    this.notaRepository = notaRepository;
    this.diarioRepository = diarioRepository;
    this.aulaRepository = aulaRepository;
  }

  public void registrarChamadaLote(
      String matriculaProfessor,
      String codigoDiario,
      String idAula,
      List<Matricula> alunosComStatus)
      throws ValidacaoException {

    Diario diario = diarioRepository.buscarPorCodigo(codigoDiario);
    if (diario == null) {
      throw new ValidacaoException("Erro: Diário '" + codigoDiario + "' não encontrado.");
    }

    if (diario.isFechado()) {
      throw new ValidacaoException("Erro: Lançamento bloqueado. O diário está FECHADO.");
    }

    if (!diario.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
      throw new ValidacaoException(
          "Erro: Acesso negado. O professor logado não é o responsável por este diário.");
    }

    String codigoDisciplina = diario.getCodigoDisciplina();
    String periodo = diario.getPeriodo();

    List<Aula> aulasDoDiario = aulaRepository.buscarPorDiario(codigoDiario);
    Aula aulaAlvo = null;
    if (aulasDoDiario != null) {
      for (Aula a : aulasDoDiario) {
        if (a.getId().equalsIgnoreCase(idAula)) {
          aulaAlvo = a;
          break;
        }
      }
    }

    if (aulaAlvo == null) {
      throw new ValidacaoException(
          "Erro: Aula '" + idAula + "' não encontrada no diário '" + codigoDiario + "'.");
    }

    String dataAula = aulaAlvo.getData();

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
              idAula,
              codigoDiario,
              dataAula,
              m.getMatriculaAluno(),
              codigoDisciplina,
              periodo,
              statusChamada));
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
    List<Turma> turmas = turmaRepository.buscarTodas();

    if (turmas != null) {
      for (Turma t : turmas) {
        if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
            && t.getPeriodo().equalsIgnoreCase(periodo)) {
          turmaExiste = true;
          break;
        }
      }
    }

    if (!turmaExiste) {
      throw new ValidacaoException(
          "Erro Analítico: A turma informada não existe no sistema corporativo para este período.");
    }

    List<Frequencia> historico =
        frequenciaRepository.buscarPorAlunoEDisciplina(matriculaAluno, codigoDisciplina, periodo);

    int totalAulas = 0;
    int presencas = 0;
    int faltas = 0;

    if (historico != null) {
      totalAulas = historico.size();
      for (Frequencia f : historico) {
        if (f.getStatus() == Frequencia.TipoFrequencia.PRESENCA) {
          presencas++;
        } else {
          faltas++;
        }
      }
    }

    double percentual = 100.0;
    if (totalAulas > 0) {
      percentual = ((double) presencas / totalAulas) * 100.0;
    }

    double n1 = 0.0;
    double n2 = 0.0;
    Nota notaAluno =
        notaRepository.buscarPorAlunoEDisciplina(matriculaAluno, codigoDisciplina, periodo);
    if (notaAluno != null) {
      n1 = notaAluno.getNota1();
      n2 = notaAluno.getNota2();
    }

    return new DesempenhoFrequencia(totalAulas, presencas, faltas, percentual, n1, n2);
  }

  public boolean verificarRiscoReprovacao(double percentualFrequencia) {
    return percentualFrequencia < 75.0;
  }
}
