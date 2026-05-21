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
        // Limpa o arquivo de dados físico antes de cada teste para garantir isolamento
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

    // ==========================================
    // CENÁRIOS DA TASK 1715 - ROBUSTEZ DA US02
    // ==========================================

    @Test
    public void testNegacaoDeAcessoParaSenhaIncorreta() throws Exception {
        // Cadastra um usuário válido na base local
        authService.cadastrarUsuario("aluno", "Bruno", "171501", "bruno@uepb.edu.br", "senhaCorreta");

        // Tenta logar usando uma senha incorreta (Garante a negação de acesso)
        try {
            authService.realizarLogin("171501", "senhaIncorreta");
            fail("Deveria ter lançado exceção por conta da senha incorreta.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Credenciais inválidas") || e.getMessage().contains("incorreta"));
        }

        // Verifica que a sessão ativa permanece vazia/nula
        assertNull(authService.getUsuarioLogado());
    }

    @Test
    public void testLoginComUsuarioInexistente() {
        // Tenta logar com uma matrícula ou e-mail que nunca foi adicionado
        try {
            authService.realizarLogin("usuario_fantasma@uepb.edu.br", "12345");
            fail("Deveria ter lançado exceção por usuário inexistente.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Credenciais inválidas") || e.getMessage().contains("não encontrado"));
        }

        assertNull(authService.getUsuarioLogado());
    }

    @Test
    public void testBloqueioDeComandosSemSessaoAtiva() {
        // Garante que o estado inicial do sistema é deslogado
        authService.realizarLogout();
        
        // Verifica se a variável global de sessão está nula, protegendo comandos restritos da CLI
        assertNull("Usuários não logados não devem ter acesso a uma sessão ativa.", authService.getUsuarioLogado());
    }
}