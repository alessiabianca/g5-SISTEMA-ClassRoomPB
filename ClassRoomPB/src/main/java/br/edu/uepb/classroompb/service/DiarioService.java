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
   * TASK 2790: Métodos para criação de Diário com validação de existência de turma
   * e validação de existência do professor responsável (RN18 - proíbe diário órfão).
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
    validarCamposObrigatorios(codigo, codigoDisciplina, periodo, matriculaProfessor, horario, sala, cargaHoraria);

    // 2. Validação da existência da turma ofertada para o período
    validarExistenciaTurma(codigoDisciplina, periodo);

    // 3. Validação do professor (RN18: Proibido diário órfão)
    validarProfessorExistente(matriculaProfessor);

    // 4. Validação de diário duplicado por código
    if (diarioRepository.buscarPorCodigo(codigo) != null) {
      throw new ValidacaoException("Erro: Já existe um diário cadastrado com o código '" + codigo + "'.");
    }

    // 5. Instanciação e Persistência
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

    if (codigo == null || codigo.trim().isEmpty()
        || codigoDisciplina == null || codigoDisciplina.trim().isEmpty()
        || periodo == null || periodo.trim().isEmpty()
        || matriculaProfessor == null || matriculaProfessor.trim().isEmpty()
        || horario == null || horario.trim().isEmpty()
        || sala == null || sala.trim().isEmpty()) {
      throw new IllegalArgumentException("Erro: Todos os campos do diário são obrigatórios.");
    }

    if (cargaHoraria <= 0) {
      throw new IllegalArgumentException("Erro: A carga horária deve ser maior que zero.");
    }
  }

  private void validarExistenciaTurma(String codigoDisciplina, String periodo) throws ValidacaoException {
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
          "Erro: A turma '" + codigoDisciplina + "' para o período '" + periodo + "' não está ofertada.");
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

  public List<Diario> listarTodos() {
    return diarioRepository.buscarTodos();
  }
}