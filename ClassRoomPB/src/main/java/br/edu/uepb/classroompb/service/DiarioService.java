package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.model.Professor;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.List;

public class DiarioService {

  private final DiarioRepository diarioRepository;
  private final TurmaRepository turmaRepository;
  private final UsuarioRepository usuarioRepository;

  public DiarioService(
      DiarioRepository diarioRepository,
      TurmaRepository turmaRepository,
      UsuarioRepository usuarioRepository) {
    if (diarioRepository == null || turmaRepository == null || usuarioRepository == null) {
      throw new IllegalArgumentException("Erro: Repositórios não podem ser nulos.");
    }
    this.diarioRepository = diarioRepository;
    this.turmaRepository = turmaRepository;
    this.usuarioRepository = usuarioRepository;
  }

  /**
   * TASK 2790 e TASK 2793: Criação de Diário com validação de turma, professor responsável (RN18) e
   * bloqueio de choque de horário de professores cruzando diários ativos (RF12).
   */
  public Diario criarDiario(
      String codigo,
      String codigoDisciplina,
      String periodo,
      String descricao,
      String matriculaProfessor,
      String horario,
      String sala,
      int cargaHoraria)
      throws ValidacaoException, ChoqueHorarioException {

    // 1. Validação de campos obrigatórios
    validarCamposObrigatorios(
        codigo, codigoDisciplina, periodo, matriculaProfessor, horario, sala, cargaHoraria);

    // 2. Validação da existência da turma ofertada para o período
    validarExistenciaTurma(codigoDisciplina, periodo);

    // 3. Validação do professor (RN18: Proibido diário órfão)
    validarProfessorExistente(matriculaProfessor);

    // 4. TASK 2793: Validação de choque de horário do professor cruzando diários ativos (RF12)
    validarChoqueHorarioProfessor(matriculaProfessor, periodo, horario);

    // 5. Validação de diário duplicado por código
    if (diarioRepository.buscarPorCodigo(codigo) != null) {
      throw new ValidacaoException(
          "Erro: Já existe um diário cadastrado com o código '" + codigo + "'.");
    }

    // 6. Instanciação e Persistência
    Diario novoDiario =
        new Diario(
            codigo,
            codigoDisciplina,
            periodo,
            descricao,
            matriculaProfessor,
            horario,
            sala,
            cargaHoraria);

    diarioRepository.salvar(novoDiario);
    return novoDiario;
  }

  private void validarCamposObrigatorios(
      String codigo,
      String codigoDisciplina,
      String periodo,
      String matriculaProfessor,
      String horario,
      String sala,
      int cargaHoraria) {

    if (codigo == null
        || codigo.trim().isEmpty()
        || codigoDisciplina == null
        || codigoDisciplina.trim().isEmpty()
        || periodo == null
        || periodo.trim().isEmpty()
        || matriculaProfessor == null
        || matriculaProfessor.trim().isEmpty()
        || horario == null
        || horario.trim().isEmpty()
        || sala == null
        || sala.trim().isEmpty()) {
      throw new IllegalArgumentException("Erro: Todos os campos do diário são obrigatórios.");
    }

    if (cargaHoraria <= 0) {
      throw new IllegalArgumentException("Erro: A carga horária deve ser maior que zero.");
    }
  }

  private void validarExistenciaTurma(String codigoDisciplina, String periodo)
      throws ValidacaoException {
    List<Turma> turmas = turmaRepository.buscarTodas();
    boolean existe = false;

    if (turmas != null) {
      for (Turma t : turmas) {
        if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
            && t.getPeriodo().equalsIgnoreCase(periodo)) {
          existe = true;
          break;
        }
      }
    }

    if (!existe) {
      throw new ValidacaoException(
          "Erro: A turma '"
              + codigoDisciplina
              + "' para o período '"
              + periodo
              + "' não está ofertada.");
    }
  }

  private void validarProfessorExistente(String matriculaProfessor) throws ValidacaoException {
    Usuario usuario = usuarioRepository.buscarPorMatricula(matriculaProfessor);

    if (usuario == null || !(usuario instanceof Professor)) {
      throw new ValidacaoException(
          "Erro (RN18): O professor de matrícula '"
              + matriculaProfessor
              + "' não foi encontrado ou não possui o perfil de Professor.");
    }
  }

  /**
   * TASK 2793 / RF12: Valida se o professor já possui outro diário alocado no mesmo período e
   * horário.
   */
  private void validarChoqueHorarioProfessor(
      String matriculaProfessor, String periodo, String horario) throws ChoqueHorarioException {

    List<Diario> diariosExistentes = diarioRepository.buscarTodos();

    if (diariosExistentes != null) {
      for (Diario d : diariosExistentes) {
        if (d.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)
            && d.getPeriodo().equalsIgnoreCase(periodo)
            && d.getHorario().equalsIgnoreCase(horario)) {
          throw new ChoqueHorarioException(
              "Erro (RF12): O professor '"
                  + matriculaProfessor
                  + "' já possui o diário '"
                  + d.getCodigo()
                  + "' cadastrado no mesmo horário ("
                  + horario
                  + ") para o período '"
                  + periodo
                  + "'.");
        }
      }
    }
  }

  public List<Diario> listarTodos() {
    return diarioRepository.buscarTodos();
  }
}
