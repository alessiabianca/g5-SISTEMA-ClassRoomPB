package br.edu.uepb.classroompb.factory;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.*;
import org.junit.Test;

public class UsuarioFactoryTest {

  @Test
  public void testCriarUsuarioValido() throws Exception {
    Usuario aluno =
        UsuarioFactory.criarUsuario("aluno", "123", "Nome", "email@test.com", "senha", "C01");
    assertTrue(aluno instanceof Aluno);

    Usuario prof =
        UsuarioFactory.criarUsuario("professor", "123", "Nome", "email@test.com", "senha");
    assertTrue(prof instanceof Professor);

    Usuario profUpper =
        UsuarioFactory.criarUsuario(" PROFESSOR ", "123", "Nome", "email@test.com", "senha");
    assertTrue(profUpper instanceof Professor);

    Usuario coord =
        UsuarioFactory.criarUsuario("coordenador", "123", "Nome", "email@test.com", "senha", "C01");
    assertTrue(coord instanceof Coordenador);

    Usuario admin =
        UsuarioFactory.criarUsuario("administrador", "123", "Nome", "email@test.com", "senha");
    assertTrue(admin instanceof Administrador);
  }

  @Test(expected = Exception.class)
  public void testCriarUsuarioNulo() throws Exception {
    UsuarioFactory.criarUsuario(null, "123", "Nome", "email@test.com", "senha");
  }

  @Test(expected = Exception.class)
  public void testCriarUsuarioInvalido() throws Exception {
    UsuarioFactory.criarUsuario("invalido", "123", "Nome", "email@test.com", "senha");
  }

  @Test
  public void testInstantiate() {
    new UsuarioFactory();
  }
}
