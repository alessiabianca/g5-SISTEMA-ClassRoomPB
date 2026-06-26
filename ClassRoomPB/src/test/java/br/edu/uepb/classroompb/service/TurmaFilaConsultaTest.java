package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class TurmaFilaConsultaTest {

    private TurmaRepository turmaRepository;
    private MatriculaRepository matriculaRepository;
    private TurmaService turmaService;

    @Before
    public void setUp() throws Exception {
        // Garante a existência da pasta data para evitar quebras em builds limpos
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Limpa os arquivos físicos para isolamento dos testes
        new File("data/turmas.txt").delete();
        new File("data/matriculas.txt").delete();

        this.turmaRepository = new TurmaRepository();
        this.matriculaRepository = new MatriculaRepository();
        
        // Injeta os repositórios necessários na inicialização do serviço
        this.turmaService = new TurmaService(turmaRepository, new PeriodoRepository(), new DisciplinaRepository());
    }

    @Test
    public void deveRetornarListaVaziaSemNullPointerExceptionSeNaoHouverFila() throws Exception {
        // Configura uma turma válida no repositório
        turmaRepository.salvar(new Turma("ES35", "PROF_Y", "2027.1", 30, "10:00-12:00", "Sala 2"));
        
        // Executa a busca para uma turma existente mas que não possui nenhuma matrícula com status ESPERA
        List<Matricula> fila = turmaService.obterListaEspera("ES35", "2027.1");
        
        // ASSERÇÕES DA TASK 2284 (Anti-NPE e tamanho zero)
        assertNotNull("A lista retornada nunca deve ser nula para evitar NullPointerException", fila);
        assertEquals("O tamanho da lista de espera deve ser zero para turmas sem fila ativa", 0, fila.size());
    }

    @Test
    public void deveRetornarContagemExataDeAlunosNaListaDeEspera() throws Exception {
        // Configura uma turma com limite de 1 vaga física
        turmaRepository.salvar(new Turma("ES35", "PROF_Y", "2027.1", 1, "10:00-12:00", "Sala 2"));
        
        // Simula registros persistidos em disco através do repositório
        matriculaRepository.salvar(new Matricula("0001", "ES35", "2027.1", Matricula.StatusMatricula.CONFIRMADA)); // Vaga física
        matriculaRepository.salvar(new Matricula("0002", "ES35", "2027.1", Matricula.StatusMatricula.ESPERA));     // 1º da Fila
        matriculaRepository.salvar(new Matricula("0003", "ES35", "2027.1", Matricula.StatusMatricula.ESPERA));     // 2º da Fila

        // Executa a consulta administrativa do coordenador
        List<Matricula> fila = turmaService.obterListaEspera("ES35", "2027.1");
        
        // ASSERÇÕES DA TASK 2284 (Validação da contagem e dados reais)
        assertNotNull(fila);
        assertEquals("A contagem exposta deve bater exatamente com os dados reais (2 alunos na fila)", 2, fila.size());
        assertEquals("O primeiro aluno da lista retornada deve ser a matrícula 0002", "0002", fila.get(0).getMatriculaAluno());
        assertEquals("O segundo aluno da lista retornada deve ser a matrícula 0003", "0003", fila.get(1).getMatriculaAluno());
    }
}
