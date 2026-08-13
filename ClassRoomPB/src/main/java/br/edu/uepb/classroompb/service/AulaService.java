package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Aula;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.repository.AulaRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.List;

/** Operacoes de aula protegidas pela imutabilidade do diario. */
public class AulaService {
  private final AulaRepository aulaRepository;
  private final DiarioRepository diarioRepository;

  public AulaService(AulaRepository aulaRepository, DiarioRepository diarioRepository) {
    this.aulaRepository = aulaRepository;
    this.diarioRepository = diarioRepository;
  }

  public void cadastrarAula(Aula aula) throws ValidacaoException {
    if (aula == null) {
      throw new ValidacaoException("Erro: Aula obrigatoria.");
    }
    validarCamposAula(aula);
    validarDiarioAberto(aula.getCodigoDiario());
    aulaRepository.salvar(aula);
  }

  public void cadastrarAula(String matriculaProfessor, Aula aula) throws ValidacaoException {
    if (aula == null) {
      throw new ValidacaoException("Erro: Aula obrigatoria.");
    }
    validarCamposAula(aula);
    Diario diario = buscarDiarioObrigatorio(aula.getCodigoDiario());
    if (!diario.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
      throw new ValidacaoException(
          "Erro: Apenas o professor responsavel pode cadastrar aulas neste diario.");
    }
    if (diario.isFechado()) {
      throw new ValidacaoException("Erro: Alteracao bloqueada. O diario esta FECHADO.");
    }
    aulaRepository.salvar(aula);
  }

  public void editarAula(Aula aulaAtualizada) throws ValidacaoException {
    if (aulaAtualizada == null) {
      throw new ValidacaoException("Erro: Aula obrigatoria.");
    }
    validarDiarioAberto(aulaAtualizada.getCodigoDiario());
    List<Aula> aulas = aulaRepository.buscarTodas();
    boolean encontrou = false;
    for (int i = 0; i < aulas.size(); i++) {
      Aula atual = aulas.get(i);
      if (atual.getId().equalsIgnoreCase(aulaAtualizada.getId())
          && atual.getCodigoDiario().equalsIgnoreCase(aulaAtualizada.getCodigoDiario())) {
        aulas.set(i, aulaAtualizada);
        encontrou = true;
        break;
      }
    }
    if (!encontrou) {
      throw new ValidacaoException("Erro: Aula nao encontrada.");
    }
    aulaRepository.atualizarArquivoCompleto(aulas);
  }

  public void excluirAula(String codigoDiario, String idAula) throws ValidacaoException {
    validarDiarioAberto(codigoDiario);
    List<Aula> aulas = aulaRepository.buscarTodas();
    boolean removeu =
        aulas.removeIf(
            aula ->
                aula.getCodigoDiario().equalsIgnoreCase(codigoDiario)
                    && aula.getId().equalsIgnoreCase(idAula));
    if (!removeu) {
      throw new ValidacaoException("Erro: Aula nao encontrada.");
    }
    aulaRepository.atualizarArquivoCompleto(aulas);
  }

  private void validarDiarioAberto(String codigoDiario) throws ValidacaoException {
    Diario diario = buscarDiarioObrigatorio(codigoDiario);
    if (diario.isFechado()) {
      throw new ValidacaoException("Erro: Alteracao bloqueada. O diario esta FECHADO.");
    }
  }

  private Diario buscarDiarioObrigatorio(String codigoDiario) throws ValidacaoException {
    Diario diario = diarioRepository.buscarPorCodigo(codigoDiario);
    if (diario == null) {
      throw new ValidacaoException("Erro: Diario nao encontrado.");
    }
    return diario;
  }

  private void validarCamposAula(Aula aula) throws ValidacaoException {
    if (aula.getId() == null
        || aula.getId().trim().isEmpty()
        || aula.getCodigoDiario() == null
        || aula.getCodigoDiario().trim().isEmpty()
        || aula.getData() == null
        || aula.getData().trim().isEmpty()
        || aula.getAssunto() == null
        || aula.getAssunto().trim().isEmpty()) {
      throw new ValidacaoException("Erro: Todos os campos da aula sao obrigatorios.");
    }
    if (aula.getQuantidadeAulas() <= 0) {
      throw new ValidacaoException("Erro: A quantidade de aulas deve ser maior que zero.");
    }
  }
}
