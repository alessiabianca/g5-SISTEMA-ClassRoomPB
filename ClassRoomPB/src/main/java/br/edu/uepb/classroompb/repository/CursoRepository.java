package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Curso;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CursoRepository implements Repository<br.edu.uepb.classroompb.model.Curso> {
  private static final String FILE_PATH = "data/cursos.txt";

  public CursoRepository() {

    File file = new File(FILE_PATH);
    if (!file.exists()) {
      try {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
          parent.mkdirs();
        }
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException("Erro ao inicializar o arquivo de armazenamento de cursos.", e);
      }
    }
  }

  public void salvar(Curso curso) throws IOException {

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
      writer.write(curso.toString());
      writer.newLine();
    }
  }

  public List<Curso> listarTodos() throws IOException {
    List<Curso> cursos = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
      String linha;
      while ((linha = reader.readLine()) != null) {
        if (linha.trim().isEmpty()) continue;
        String[] dados = linha.split(";");
        if (dados.length >= 2) {
          Curso curso = new Curso(dados[0], dados[1]);
          cursos.add(curso);
        }
      }
    }
    return cursos;
  }

  /** Implementa o contrato Repository<Curso>. Alias para listarTodos(). */
  @Override
  public List<Curso> buscarTodas() {
    try {
      return listarTodos();
    } catch (IOException e) {
      System.err.println("Erro ao listar cursos: " + e.getMessage());
      return new ArrayList<>();
    }
  }

  public Curso buscarPorCodigo(String codigo) throws IOException {
    List<Curso> cursos = listarTodos();
    for (Curso curso : cursos) {
      if (curso.getCodigo().equalsIgnoreCase(codigo)) {
        return curso;
      }
    }
    return null;
  }
}
