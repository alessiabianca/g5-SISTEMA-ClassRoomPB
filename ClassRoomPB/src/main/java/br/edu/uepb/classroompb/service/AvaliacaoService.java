package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Avaliacao;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.repository.AvaliacaoRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.List;
import java.util.UUID;

public class AvaliacaoService {
  private final AvaliacaoRepository avaliacaoRepository;
  private final DiarioRepository diarioRepository;

  public AvaliacaoService(
      AvaliacaoRepository avaliacaoRepository, DiarioRepository diarioRepository) {
    this.avaliacaoRepository = avaliacaoRepository;
    this.diarioRepository = diarioRepository;
  }

  public Avaliacao cadastrarAvaliacao(
      String matriculaProfessor,
      String codigoDiario,
      String descricao,
      int etapa,
      double peso,
      double notaMaxima)
      throws ValidacaoException {

    Diario diario = diarioRepository.buscarPorCodigo(codigoDiario);
    if (diario == null) {
      throw new ValidacaoException("Diário não encontrado.");
    }

    if (!diario.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
      throw new ValidacaoException(
          "Ação não permitida: Você não é o professor responsável por este diário.");
    }

    if (diario.isFechado()) {
      throw new ValidacaoException(
          "O Diário de Classe já está encerrado e não permite novas avaliações.");
    }

    if (etapa < 1 || etapa > 3) {
      throw new ValidacaoException("Etapa inválida. Use 1, 2 ou 3.");
    }

    if (peso <= 0.0) {
      throw new ValidacaoException("O peso da avaliação deve ser maior que 0.");
    }

    if (notaMaxima <= 0.0 || notaMaxima > 10.0) {
      throw new ValidacaoException("A nota máxima da avaliação deve estar entre 0.1 e 10.0.");
    }

    List<Avaliacao> avaliacoes = avaliacaoRepository.buscarPorDiario(codigoDiario);
    for (Avaliacao av : avaliacoes) {
      if (av.getEtapa() == etapa) {
        throw new ValidacaoException(
            "Já existe uma avaliação cadastrada para a etapa " + etapa + " neste diário.");
      }
    }

    String id = UUID.randomUUID().toString().substring(0, 8);
    Avaliacao novaAvaliacao = new Avaliacao(id, codigoDiario, descricao, etapa, peso, notaMaxima);
    avaliacaoRepository.salvar(novaAvaliacao);

    return novaAvaliacao;
  }

  public List<Avaliacao> listarAvaliacoes(String codigoDiario) {
    return avaliacaoRepository.buscarPorDiario(codigoDiario);
  }
}
