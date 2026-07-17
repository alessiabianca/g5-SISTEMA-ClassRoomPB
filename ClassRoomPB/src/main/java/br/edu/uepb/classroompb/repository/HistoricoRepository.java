package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Historico;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoRepository {
  private static final String ARQUIVO = "data/historico.txt";

  public HistoricoRepository() {
    File dir = new File("data");
    if (!dir.exists()) {
      dir.mkdirs();
    }
    File file = new File(ARQUIVO);
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void salvarLote(List<Historico> historicos) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, true))) {
      for (Historico h : historicos) {
        bw.write(h.toString());
        bw.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public List<Historico> buscarPorAluno(String matriculaAluno) {
    List<Historico> resultado = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(ARQUIVO))) {
      String linha;
      while ((linha = br.readLine()) != null) {
        if (linha.trim().isEmpty()) {
          continue;
        }
        Historico h = Historico.fromString(linha);
        if (h.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)) {
          resultado.add(h);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return resultado;
  }

  public boolean existe(String matriculaAluno, String codigoDisciplina, String periodo) {
    for (Historico historico : buscarPorAluno(matriculaAluno)) {
      if (historico.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && historico.getPeriodo().equalsIgnoreCase(periodo)) {
        return true;
      }
    }
    return false;
  }
}
