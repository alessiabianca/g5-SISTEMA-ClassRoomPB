package br.edu.uepb.classroompb.repository;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Aluno;
import br.edu.uepb.classroompb.model.Professor;
import br.edu.uepb.classroompb.model.Usuario;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class UsuarioRepositoryTest {
  private UsuarioRepository repository;
  private static final String FILE_PATH = "usuarios.dat";

  @Before
  public void setUp() {
    File f = new File(FILE_PATH);
    if (f.exists()) f.delete();
    repository = new UsuarioRepository();
  }

  @Test
  public void testSalvarEBuscarTodos() throws Exception {
    Aluno a = new Aluno("123", "Nome", "email", "senha", "C01");
    Professor p = new Professor("456", "Prof", "prof@email", "senha");
    
    repository.salvar(a);
    repository.salvar(p);
    
    List<Usuario> usuarios = repository.buscarTodos();
    assertEquals(2, usuarios.size());
    
    Usuario lida = repository.buscarPorMatricula("123");
    assertEquals("ALUNO", lida.getPerfil());
    assertEquals("123", lida.getMatricula());
    assertEquals("Nome", lida.getNome());
    assertEquals("email", lida.getEmail());
    assertEquals("senha", lida.getSenha());
    assertEquals("C01", lida.getCodigoCurso());
    
    Usuario lida2 = repository.buscarPorMatricula("456");
    assertEquals("PROFESSOR", lida2.getPerfil());
    assertEquals("456", lida2.getMatricula());
    assertNull(lida2.getCodigoCurso());
  }

  @Test
  public void testAtualizarCurso() throws Exception {
    Aluno a = new Aluno("123", "Nome", "email", "senha", "C01");
    repository.salvar(a);
    
    boolean atualizado = repository.atualizarCurso("123", "C02");
    assertTrue(atualizado);
    
    Usuario lida = repository.buscarPorMatricula("123");
    assertEquals("C02", lida.getCodigoCurso());
  }
}
