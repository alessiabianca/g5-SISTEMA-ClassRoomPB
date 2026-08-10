package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Disciplina;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisciplinaRepository implements Repository<br.edu.uepb.classroompb.model.Disciplina> {
  private static final String FILE_PATH = "data/disciplinas.txt";

  public DisciplinaRepository() {
    File file = new File(FILE_PATH);
    if (!file.exists()) {
      try {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
          parent.mkdirs();
        }
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException("Erro ao inicializar o arquivo de disciplinas.", e);
      }
    }
  }

  public void salvar(Disciplina disciplina) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
      writer.write(disciplina.toString());
      writer.newLine();
    }
  }

  public List<Disciplina> listarTodas() throws IOException {
    List<Disciplina> disciplinas = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
      String linha;
      while ((linha = reader.readLine()) != null) {
        if (linha.trim().isEmpty()) continue;
        String[] dados = linha.split(";");
        if (dados.length >= 5) {
          String codigo = dados[0];
          String nome = dados[1];
          int cargaHoraria = Integer.parseInt(dados[2]);
          int creditos = Integer.parseInt(dados[3]);

          List<String> preRequisitos = new ArrayList<>();
          if (!dados[4].equals("NENHUM")) {
            preRequisitos.addAll(Arrays.asList(dados[4].split(",")));
          }

          disciplinas.add(new Disciplina(codigo, nome, cargaHoraria, creditos, preRequisitos));
        }
      }
    }
    return disciplinas;
  }

  public Disciplina buscarPorCodigo(String codigo) throws IOException {
    List<Disciplina> disciplinas = listarTodas();
    for (Disciplina d : disciplinas) {
      if (d.getCodigo().equalsIgnoreCase(codigo)) {
        return d;
      }
    }
    return null;
  }

  /** Implementa o contrato Repository<Disciplina>. Alias para listarTodas(). */
  @Override
  public List<Disciplina> buscarTodas() {
    try {
      return listarTodas();
    } catch (IOException e) {
      System.err.println("Erro ao listar disciplinas: " + e.getMessage());
      return new ArrayList<>();
    }
  }
}
