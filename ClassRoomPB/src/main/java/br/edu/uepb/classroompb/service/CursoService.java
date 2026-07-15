package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Curso;
import br.edu.uepb.classroompb.repository.CursoRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.IOException;

public class CursoService {
  private final CursoRepository cursoRepository;

  public CursoService(CursoRepository cursoRepository) {
    this.cursoRepository = cursoRepository;
  }

  public void cadastrarCurso(String codigo, String nome, String papelUsuarioLogado)
      throws ValidacaoException, IOException {
    // Critério de Aceitação: Apenas administradores podem cadastrar cursos
    if (papelUsuarioLogado == null || !papelUsuarioLogado.equalsIgnoreCase("ADMINISTRADOR")) {
      throw new ValidacaoException("Acesso negado: Apenas administradores podem cadastrar cursos.");
    }

    // Critério de Aceitação: O sistema deve permitir cadastrar um curso com código e nome
    // (Validação de campos)
    if (codigo == null || codigo.trim().isEmpty()) {
      throw new ValidacaoException("O código do curso é obrigatório.");
    }
    if (nome == null || nome.trim().isEmpty()) {
      throw new ValidacaoException("O nome do curso é obrigatório.");
    }

    // Critério de Aceitação: O sistema não deve permitir código duplicado
    Curso cursoExistente = cursoRepository.buscarPorCodigo(codigo.trim());
    if (cursoExistente != null) {
      throw new ValidacaoException(
          "Erro: Já existe um curso cadastrado com o código '" + codigo + "'.");
    }

    Curso novoCurso = new Curso(codigo.trim(), nome.trim());
    cursoRepository.salvar(novoCurso);
  }
}
