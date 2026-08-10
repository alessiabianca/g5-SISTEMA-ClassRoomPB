package br.edu.uepb.classroompb.service;

import static org.junit.Assert.*;

import br.edu.uepb.classroompb.model.Turma;
import org.junit.Test;

public class TurmaRefatoradaRF11Test {

  @Test
  public void deveCriarTurmaApenasComDadosDeOfertaAcademica() {
    Turma turma = new Turma("ES01", "2026.1", 30);

    assertEquals("ES01", turma.getCodigoDisciplina());
    assertEquals("2026.1", turma.getPeriodo());
    assertEquals(30, turma.getVagas());
    assertEquals(0, turma.getVagasOcupadas());
  }

  @Test
  public void deveCalcularVagasDisponiveis() {
    Turma turma = new Turma("ES01", "2026.1", 30, 28);

    assertEquals(2, turma.getVagas() - turma.getVagasOcupadas());

    turma.setVagasOcupadas(30);
    assertEquals(0, turma.getVagas() - turma.getVagasOcupadas());
  }

  @Test
  public void deveRepresentarFormatacaoEstrategicaParaRelatorios() {
    Turma turma = new Turma("BD01", "2026.2", 40);
    assertNotNull(turma.toString());
  }
}
