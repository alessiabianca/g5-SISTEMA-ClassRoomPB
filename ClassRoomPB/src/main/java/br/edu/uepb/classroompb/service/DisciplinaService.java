package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Disciplina;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaService {
  private final DisciplinaRepository disciplinaRepository;

  public DisciplinaService(DisciplinaRepository disciplinaRepository) {
    this.disciplinaRepository = disciplinaRepository;
  }

  public void cadastrarDisciplina(
      String codigo,
      String nome,
      int cargaHoraria,
      int creditos,
      List<String> preRequisitosInputs,
      String papelUsuarioLogado)
      throws ValidacaoException, IOException {

    // Critério de Aceitação: Apenas coordenadores podem cadastrar disciplinas
    if (papelUsuarioLogado == null || !papelUsuarioLogado.equalsIgnoreCase("COORDENADOR")) {
      throw new ValidacaoException(
          "Acesso negado: Apenas coordenadores podem cadastrar disciplinas.");
    }

    // Validações de campos obrigatórios
    if (codigo == null || codigo.trim().isEmpty()) {
      throw new ValidacaoException("O código da disciplina é obrigatório.");
    }
    if (nome == null || nome.trim().isEmpty()) {
      throw new ValidacaoException("O nome da disciplina é obrigatório.");
    }

    // Critério de Aceitação: Validar carga horária e créditos positivos
    if (cargaHoraria <= 0) {
      throw new ValidacaoException("A carga horária deve ser um valor positivo.");
    }
    if (creditos <= 0) {
      throw new ValidacaoException("Os créditos devem ser um valor positivo.");
    }

    // Critério de Aceitação: O sistema não deve permitir código duplicado
    Disciplina disciplinaExistente = disciplinaRepository.buscarPorCodigo(codigo.trim());
    if (disciplinaExistente != null) {
      throw new ValidacaoException(
          "Erro: Já existe uma disciplina cadastrada com o código '" + codigo + "'.");
    }

    // Processamento e validação dos pré-requisitos opcionais
    List<String> preRequisitosValidados = new ArrayList<>();
    if (preRequisitosInputs != null) {
      for (String prCodigo : preRequisitosInputs) {
        String prLimpo = prCodigo.trim();
        if (prLimpo.isEmpty()) continue;

        // Valida se o pré-requisito já está cadastrado no sistema
        Disciplina prExistente = disciplinaRepository.buscarPorCodigo(prLimpo);
        if (prExistente == null) {
          throw new ValidacaoException(
              "Erro: A disciplina de pré-requisito '" + prLimpo + "' não está cadastrada.");
        }

        if (prLimpo.equalsIgnoreCase(codigo.trim())) {
          throw new ValidacaoException(
              "Erro: Uma disciplina não pode ter a si mesma como pré-requisito.");
        }

        if (!preRequisitosValidados.contains(prLimpo)) {
          preRequisitosValidados.add(prLimpo);
        }
      }
    }

    Disciplina novaDisciplina =
        new Disciplina(codigo.trim(), nome.trim(), cargaHoraria, creditos, preRequisitosValidados);
    disciplinaRepository.salvar(novaDisciplina);
  }
}
