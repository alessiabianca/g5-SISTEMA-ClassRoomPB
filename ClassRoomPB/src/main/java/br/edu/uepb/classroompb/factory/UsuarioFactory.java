package br.edu.uepb.classroompb.factory;

import br.edu.uepb.classroompb.model.*;

public class UsuarioFactory {

  /**
   * Padrão Factory: Centraliza e isola a lógica de criação de perfis. Facilita a manutenção e
   * expansão do sistema (Desacoplamento).
   */
  public static Usuario criarUsuario(
      String tipo, String matricula, String nome, String email, String senha) throws Exception {
    if (tipo == null) {
      throw new Exception("Erro: Tipo de perfil não pode ser nulo.");
    }

    return switch (tipo.toLowerCase().trim()) {
      case "aluno" -> new Aluno(matricula, nome, email, senha);
      case "professor" -> new Professor(matricula, nome, email, senha);
      case "coordenador" -> new Coordenador(matricula, nome, email, senha);
      case "administrador" -> new Administrador(matricula, nome, email, senha);
      default -> throw new Exception("Erro: Tipo de perfil '" + tipo + "' desconhecido.");
    };
  }
}
