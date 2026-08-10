package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Frequencia;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FrequenciaRepository implements Repository<br.edu.uepb.classroompb.model.Frequencia> {
  private static final String FILE_PATH = "data/frequencias.txt";

  public FrequenciaRepository() {
    try {
      File file = new File(FILE_PATH);
      if (file.getParentFile() != null && !file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      if (!file.exists()) {
        file.createNewFile();
      }
    } catch (IOException e) {
      System.err.println("Erro crítico ao inicializar o diário de frequências: " + e.getMessage());
    }
  }

  /** Persiste uma única frequência (atalho para salvarLote com um elemento). */
  public void salvar(Frequencia frequencia) {
    salvarLote(java.util.Collections.singletonList(frequencia));
  }

  public void salvarLote(List<Frequencia> frequencias) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
      for (Frequencia f : frequencias) {
        bw.write(f.toString());
        bw.newLine();
      }
    } catch (IOException e) {
      System.err.println("Erro ao persistir frequências em lote: " + e.getMessage());
    }
  }

  public List<Frequencia> buscarTodas() {
    List<Frequencia> lista = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
      String linha;
      while ((linha = br.readLine()) != null) {
        if (linha.trim().isEmpty()) continue;
        String[] partes = linha.split(";");
        if (partes.length >= 7) {
          Frequencia.TipoFrequencia statusEnum =
              Frequencia.TipoFrequencia.valueOf(partes[6].toUpperCase().trim());
          lista.add(
              new Frequencia(
                  partes[0], partes[1], partes[2], partes[3], partes[4], partes[5], statusEnum));
        }
      }
    } catch (IOException e) {
      System.err.println("Erro ao ler histórico de frequências: " + e.getMessage());
    }
    return lista;
  }

  public List<Frequencia> buscarPorAlunoEDisciplina(
      String matriculaAluno, String codigoDisciplina, String periodo) {
    List<Frequencia> filtradas = new ArrayList<>();
    List<Frequencia> todas = buscarTodas();

    for (Frequencia f : todas) {
      if (f.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && f.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && f.getPeriodo().equalsIgnoreCase(periodo)) {
        filtradas.add(f);
      }
    }
    return filtradas;
  }
}
