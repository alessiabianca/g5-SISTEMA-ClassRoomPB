package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Curso;
import br.edu.uepb.classroompb.repository.CursoRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class CursoServiceTest {
  private CursoRepository cursoRepository;
  private CursoService cursoService;
  private static final String FILE_PATH = "data/cursos.txt";

  @Before
  public void setUp() {
    // Limpa o ambiente limpando ou deletando o arquivo de dados de teste antes de cada execução
    File file = new File(FILE_PATH);
    if (file.exists()) {
      file.delete();
    }
    cursoRepository = new CursoRepository();
    cursoService = new CursoService(cursoRepository);
  }

  @Test
  public void deveCadastrarCursoComSucesso() throws Exception {
    cursoService.cadastrarCurso("CC", "Ciência da Computação", "ADMINISTRADOR");

    Curso cadastrado = cursoRepository.buscarPorCodigo("CC");
    assertNotNull("O curso deveria ter sido cadastrado no arquivo.", cadastrado);
    assertEquals("Ciência da Computação", cadastrado.getNome());
  }

  @Test
  public void deveLancarExcecaoQuandoUsuarioNaoForAdmin() throws IOException {
    try {
      cursoService.cadastrarCurso("SI", "Sistemas de Informação", "ALUNO");
      fail("Deveria ter lançado ValidacaoException por falta de privilégios.");
    } catch (ValidacaoException e) {
      assertEquals("Acesso negado: Apenas administradores podem cadastrar cursos.", e.getMessage());
    }
  }

  @Test
  public void deveLancarExcecaoQuandoCodigoForDuplicado() throws Exception {
    cursoService.cadastrarCurso("DIR", "Direito", "ADMINISTRADOR");

    try {
      cursoService.cadastrarCurso("DIR", "Direito Noturno", "ADMINISTRADOR");
      fail("Deveria ter lançado ValidacaoException por código duplicado.");
    } catch (ValidacaoException e) {
      assertTrue(e.getMessage().contains("Já existe um curso cadastrado com o código"));
    }
  }

  @Test
  public void deveLancarExcecaoQuandoCamposForemInvalidos() throws IOException {
    try {
      cursoService.cadastrarCurso("", "Engenharia", "ADMINISTRADOR");
      fail("Deveria ter validado código vazio.");
    } catch (ValidacaoException e) {
      assertEquals("O código do curso é obrigatório.", e.getMessage());
    }

    try {
      cursoService.cadastrarCurso("ENG", "   ", "ADMINISTRADOR");
      fail("Deveria ter validado nome vazio.");
    } catch (ValidacaoException e) {
      assertEquals("O nome do curso é obrigatório.", e.getMessage());
    }
  }
}
