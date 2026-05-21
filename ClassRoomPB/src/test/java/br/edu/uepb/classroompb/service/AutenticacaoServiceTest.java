package br.edu.uepb.classroompb.service;

import org.junit.Before;
import org.junit.Test;
import java.io.File;
import br.edu.uepb.classroompb.service.exception.UsuarioJaExisteException;
import static org.junit.Assert.*;

public class AutenticacaoServiceTest {
    private AutenticacaoService authService;

    @Before
    public void setUp() {
        File file = new File("usuarios.dat");
        if (file.exists()) {
            file.delete();
        }
        authService = AutenticacaoService.getInstancia();
        authService.realizarLogout();
    }

    @Test
    public void testCadastroSucessoEPerfil() throws Exception {
        authService.cadastrarUsuario("aluno", "Alessia", "202601", "alessia@uepb.edu.br", "senha123");
        
        authService.realizarLogin("202601", "senha123");
        assertNotNull(authService.getUsuarioLogado());
        assertEquals("ALUNO", authService.getUsuarioLogado().getPerfil());
    }

    @Test
    public void testImpedimentoDuplicidadeMatricula() throws Exception {
        authService.cadastrarUsuario("professor", "Carlos", "9999", "carlos@uepb.edu.br", "123");

        // Atualizado para validar o lançamento da classe de exceção correta
        try {
            authService.cadastrarUsuario("aluno", "Mariana", "9999", "mariana@uepb.edu.br", "456");
            fail("Deveria ter lançado UsuarioJaExisteException.");
        } catch (UsuarioJaExisteException e) {
            assertTrue(e.getMessage().contains("já está cadastrado"));
        }
    }

    @Test
    public void testImpedimentoDuplicidadeEmail() throws Exception {
        authService.cadastrarUsuario("coordenador", "Paula", "8888", "paula@uepb.edu.br", "123");

        // Atualizado para validar o lançamento da classe de exceção correta
        try {
            authService.cadastrarUsuario("aluno", "Lucas", "7777", "paula@uepb.edu.br", "456");
            fail("Deveria ter lançado UsuarioJaExisteException.");
        } catch (UsuarioJaExisteException e) {
            assertTrue(e.getMessage().contains("já está cadastrado"));
        }
    }

    @Test
    public void testEntradasVaziasEInvalidas() {
        try {
            authService.cadastrarUsuario("aluno", "", "111", "email@test.com", "senha");
            fail("Deveria ter rejeitado o nome em branco.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Todos os campos são obrigatórios"));
        }
    }
}