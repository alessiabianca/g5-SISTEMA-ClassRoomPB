package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Disciplina;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class DisciplinaServiceTest {
    private DisciplinaRepository disciplinaRepository;
    private DisciplinaService dService;
    private static final String FILE_PATH = "data/disciplinas.txt";

    @Before
    public void setUp() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
        disciplinaRepository = new DisciplinaRepository();
        dService = new DisciplinaService(disciplinaRepository);
    }

    @Test
    public void deveCadastrarDisciplinaComSucesso() throws Exception {
        List<String> preRequisitos = new ArrayList<>();
        
        dService.cadastrarDisciplina("P1", "Programação I", 60, 4, preRequisitos, "COORDENADOR");
        
        Disciplina cadastrada = disciplinaRepository.buscarPorCodigo("P1");
        assertNotNull("A disciplina deveria constar na persistência.", cadastrada);
        assertEquals("Programação I", cadastrada.getNome());
        assertEquals(60, cadastrada.getCargaHoraria());
    }

    @Test
    public void deveCadastrarComPreRequisitosValidos() throws Exception {
        dService.cadastrarDisciplina("P1", "Programação I", 60, 4, null, "COORDENADOR");
        
        List<String> preReqs = new ArrayList<>();
        preReqs.add("P1");
        
        dService.cadastrarDisciplina("P2", "Programação II", 60, 4, preReqs, "COORDENADOR");
        
        Disciplina p2 = disciplinaRepository.buscarPorCodigo("P2");
        assertEquals(1, p2.getPreRequisitosCodigos().size());
        assertEquals("P1", p2.getPreRequisitosCodigos().get(0));
    }

    @Test
    public void deveLancarExcecaoQuandoUsuarioNaoForCoordenador() throws IOException {
        try {
            dService.cadastrarDisciplina("ALEST", "Álgebra Linear", 60, 4, null, "ALUNO");
            fail("Deveria falhar pois ALUNO não é COORDENADOR.");
        } catch (ValidacaoException e) {
            assertEquals("Acesso negado: Apenas coordenadores podem cadastrar disciplinas.", e.getMessage());
        }
    }

   @Test
   public void deveLancarExcecaoParaValoresNegativosOuZerar() throws IOException {
    try {
        dService.cadastrarDisciplina("ED", "Estrutura de Dados", 0, 4, null, "COORDENADOR");
        fail("Carga horária zero deve ser inválida.");
    } catch (ValidacaoException e) {
        // Corrigido de "Aarga" para "A carga" para bater exatamente com o Service
        assertEquals("A carga horária deve ser um valor positivo.", e.getMessage());
    }

    try {
        dService.cadastrarDisciplina("ED", "Estrutura de Dados", 60, -1, null, "COORDENADOR");
        fail("Créditos negativos devem ser inválidos.");
    } catch (ValidacaoException e) {
        assertEquals("Os créditos devem ser um valor positivo.", e.getMessage());
    }
}
    @Test
    public void deveLancarExcecaoParaCodigoDuplicado() throws Exception {
        dService.cadastrarDisciplina("WEB", "Desenvolvimento Web", 60, 4, null, "COORDENADOR");
        
        try {
            dService.cadastrarDisciplina("WEB", "Web Avançado", 90, 6, null, "COORDENADOR");
            fail("Código duplicado não deve ser aceito.");
        } catch (ValidacaoException e) {
            assertTrue(e.getMessage().contains("Já existe uma disciplina cadastrada com o código"));
        }
    }

    @Test
    public void deveLancarExcecaoSePreRequisitoNaoExistir() throws IOException {
        List<String> inexistente = new ArrayList<>();
        inexistente.add("FALSA123");
        
        try {
            dService.cadastrarDisciplina("BD", "Banco de Dados", 60, 4, inexistente, "COORDENADOR");
            fail("Não deve permitir vincular pré-requisito fantasma.");
        } catch (ValidacaoException e) {
            assertTrue(e.getMessage().contains("não está cadastrada"));
        }
    }
}