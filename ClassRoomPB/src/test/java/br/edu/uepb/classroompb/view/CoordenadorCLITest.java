package br.edu.uepb.classroompb.view;

import org.junit.Test;
import java.util.ArrayList;
import br.edu.uepb.classroompb.model.*;
import br.edu.uepb.classroompb.repository.*;
import br.edu.uepb.classroompb.service.*;

public class CoordenadorCLITest {
    @Test
    public void testProcessar() throws Exception {
        TurmaRepository tr = new TurmaRepository();
        PeriodoRepository pr = new PeriodoRepository();
        DisciplinaRepository dr = new DisciplinaRepository();
        MatriculaRepository mr = new MatriculaRepository();
        FrequenciaRepository fr = new FrequenciaRepository();
        NotaRepository nr = new NotaRepository();
        HistoricoRepository hr = new HistoricoRepository();
        UsuarioRepository ur = new UsuarioRepository();
        
        pr.salvar(new Periodo("P01", "ATIVO"));
        dr.salvar(new Disciplina("D01", "Nome", 60, 4, new ArrayList<>()));
        tr.salvar(new Turma("D01", "PROF1", "P01", 40, "SEG", "S01"));
        
        TurmaService ts = new TurmaService(tr, pr, dr);
        FrequenciaService fs = new FrequenciaService(tr, mr, fr, nr);
        SituacaoAcademicaService sas = new SituacaoAcademicaService(nr, fs);
        HistoricoService hs = new HistoricoService(hr, mr, tr, sas, fs);
        
        AutenticacaoService auth = AutenticacaoService.getInstancia();
        try {
            auth.cadastrarUsuario("coordenador", "Nome", "CO123", "co123@test", "senha", "C01");
        } catch (Exception e) {}
        auth.realizarLogin("CO123", "senha");
        
        CoordenadorCLI cli = new CoordenadorCLI(ts, hs, ur);
        
        cli.processar(null);
        cli.processar("   ");
        cli.processar("invalido");
        
        cli.processar("cadastrarDisciplina D02 Mat 60 4");
        cli.processar("cadastrarDisciplina");
        
        cli.processar("ofertarTurma D01 PROF1 P01 40 SEG-10M SALA01");
        cli.processar("ofertarTurma");
        
        cli.processar("cancelarTurma D01 P01");
        cli.processar("cancelarTurma");
        
        cli.processar("exibirListaEspera D01 P01");
        cli.processar("exibirListaEspera");
        
        cli.processar("consultarHistoricoAluno 12345");
        cli.processar("consultarHistoricoAluno");
        
        cli.processar("gerarRelatorioOcupacaoVagas P01");
        cli.processar("gerarRelatorioOcupacaoVagas");
        
        cli.processar("gerarRelatorioReprovacaoPorDisciplina P01");
        cli.processar("gerarRelatorioReprovacaoPorDisciplina");
    }
}
