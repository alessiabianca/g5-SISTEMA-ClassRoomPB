package br.edu.uepb.classroompb.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HistoricoTest {

  @Test
  public void deveCriarHistoricoComSucesso() {
    Historico historico =
        new Historico(
            "ALUNO1", "2026.1", "P1", "PROF1", 9.5, 100.0, StatusAcademico.APROVADO);

    assertEquals("ALUNO1", historico.getMatriculaAluno());
    assertEquals("2026.1", historico.getPeriodo());
    assertEquals("P1", historico.getCodigoDisciplina());
    assertEquals("PROF1", historico.getMatriculaProfessor());
    assertEquals(9.5, historico.getMediaFinal(), 0.001);
    assertEquals(100.0, historico.getPercentualFrequencia(), 0.001);
    assertEquals(StatusAcademico.APROVADO, historico.getStatus());
  }

  @Test
  public void deveConverterParaString() {
    Historico historico =
        new Historico(
            "ALUNO1", "2026.1", "P1", "PROF1", 9.5, 100.0, StatusAcademico.APROVADO);
    String str = historico.toString();
    assertEquals("2026.1;P1;PROF1;9.5;100.0;APROVADO;ALUNO1", str);
  }

  @Test
  public void deveConverterDeString() {
    String linha = "2026.1;P1;PROF1;9.5;100.0;APROVADO;ALUNO1";
    Historico historico = Historico.fromString(linha);
    assertEquals("ALUNO1", historico.getMatriculaAluno());
    assertEquals("2026.1", historico.getPeriodo());
    assertEquals("P1", historico.getCodigoDisciplina());
    assertEquals("PROF1", historico.getMatriculaProfessor());
    assertEquals(9.5, historico.getMediaFinal(), 0.001);
    assertEquals(100.0, historico.getPercentualFrequencia(), 0.001);
    assertEquals(StatusAcademico.APROVADO, historico.getStatus());
  }

  @Test
  public void deveConverterDeStringComSeisPartes() {
    String linha = "ALUNO1;P1;2026.1;9.5;100.0;APROVADO";
    Historico historico = Historico.fromString(linha);
    assertEquals("ALUNO1", historico.getMatriculaAluno());
    assertEquals("2026.1", historico.getPeriodo());
    assertEquals("P1", historico.getCodigoDisciplina());
    assertEquals("NAO_INFORMADO", historico.getMatriculaProfessor());
    assertEquals(9.5, historico.getMediaFinal(), 0.001);
    assertEquals(100.0, historico.getPercentualFrequencia(), 0.001);
    assertEquals(StatusAcademico.APROVADO, historico.getStatus());
  }

  @Test(expected = IllegalArgumentException.class)
  public void deveLancarErroParaStringInvalida() {
    Historico.fromString("2026.1;P1;ALUNO1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void deveLancarErroParaMatriculaVazia() {
    new Historico("", "2026.1", "P1", "PROF1", 9.5, 100.0, StatusAcademico.APROVADO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deveLancarErroParaStatusNulo() {
    new Historico("A1", "2026.1", "P1", "PROF1", 9.5, 100.0, null);
  }

  @Test
  public void deveUsarSetters() {
    Historico historico =
        new Historico(
            "A1", "2026.1", "P1", "PROF1", 9.5, 100.0, StatusAcademico.APROVADO);
    historico.setMatriculaAluno("B2");
    historico.setCodigoDisciplina("P2");
    historico.setPeriodo("2027.1");
    historico.setMatriculaProfessor("PROF2");
    historico.setMediaFinal(5.0);
    historico.setPercentualFrequencia(50.0);
    historico.setStatus(StatusAcademico.REPROVADO_NOTA);

    assertEquals("B2", historico.getMatriculaAluno());
    assertEquals("2027.1", historico.getPeriodo());
    assertEquals("P2", historico.getCodigoDisciplina());
    assertEquals("PROF2", historico.getMatriculaProfessor());
    assertEquals(5.0, historico.getMediaFinal(), 0.001);
    assertEquals(50.0, historico.getPercentualFrequencia(), 0.001);
    assertEquals(StatusAcademico.REPROVADO_NOTA, historico.getStatus());
  }
}
