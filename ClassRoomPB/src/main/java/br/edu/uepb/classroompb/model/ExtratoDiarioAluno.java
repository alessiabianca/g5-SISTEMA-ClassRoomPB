package br.edu.uepb.classroompb.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Visao isolada de um aluno sobre um diario em que possui matricula confirmada. */
public class ExtratoDiarioAluno {
  private final Matricula matricula;
  private final Diario diario;
  private final List<Aula> aulas;
  private final List<Frequencia> frequencias;
  private final List<NotaAvaliacaoDiario> notasAvaliacoes;
  private final double mediaParcial;

  public ExtratoDiarioAluno(
      Matricula matricula,
      Diario diario,
      List<Aula> aulas,
      List<Frequencia> frequencias,
      List<NotaAvaliacaoDiario> notasAvaliacoes,
      double mediaParcial) {
    this.matricula = matricula;
    this.diario = diario;
    this.aulas = Collections.unmodifiableList(new ArrayList<>(aulas));
    this.frequencias = Collections.unmodifiableList(new ArrayList<>(frequencias));
    this.notasAvaliacoes = Collections.unmodifiableList(new ArrayList<>(notasAvaliacoes));
    this.mediaParcial = mediaParcial;
  }

  public String getMatriculaAluno() {
    return matricula.getMatriculaAluno();
  }

  public Matricula getMatricula() {
    return matricula;
  }

  public Diario getDiario() {
    return diario;
  }

  public List<Aula> getAulas() {
    return aulas;
  }

  public List<Frequencia> getFrequencias() {
    return frequencias;
  }

  public List<NotaAvaliacaoDiario> getNotasAvaliacoes() {
    return notasAvaliacoes;
  }

  public double getMediaParcial() {
    return mediaParcial;
  }
}
