package br.edu.uepb.classroompb.service;

import org.junit.Before;
import org.junit.Test;
import java.io.File;
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
        // Garante a cobertura para a lógica de criação de perfis
        authService.cadastrarUsuario("aluno", "Alessia", "202601", "alessia@uepb.edu.br", "senha123");
        
        authService.realizarLogin("202601", "senha123");
        assertNotNull(authService.getUsuarioLogado());
        assertEquals("ALUNO", authService.getUsuarioLogado().getPerfil());
    }

    @Test
    public void testImpedimentoDuplicidadeMatricula() throws Exception {
        authService.cadastrarUsuario("professor", "Carlos", "9999", "carlos@uepb.edu.br", "123");

        // Tenta cadastrar outro usuário com a mesma matrícula (RF04)
        try {
            authService.cadastrarUsuario("aluno", "Mariana", "9999", "mariana@uepb.edu.br", "456");
            fail("Deveria ter lançado uma exceção de duplicidade de matrícula.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("já está cadastrado"));
        }
    }

    @Test
    public void testImpedimentoDuplicidadeEmail() throws Exception {
        authService.cadastrarUsuario("coordenador", "Paula", "8888", "paula@uepb.edu.br", "123");

        // Tenta cadastrar outro usuário com o mesmo e-mail (RF04)
        try {
            authService.cadastrarUsuario("aluno", "Lucas", "7777", "paula@uepb.edu.br", "456");
            fail("Deveria ter lançado uma exceção de duplicidade de e-mail.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("já está cadastrado"));
        }
    }

    @Test
    public void testEntradasVaziasEInvalidas() {
        // Valida se o sistema rejeita strings vazias ou nulas
        try {
            authService.cadastrarUsuario("aluno", "", "111", "email@test.com", "senha");
            fail("Deveria ter rejeitado o nome em branco.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Todos os campos são obrigatórios"));
        }
    }
}