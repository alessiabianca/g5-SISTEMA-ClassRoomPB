package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Historico;
import br.edu.uepb.classroompb.model.StatusAcademico;
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
        String[] partes = linha.split(";");
        if (partes.length == 6 && partes[0].equalsIgnoreCase(matriculaAluno)) {
          Historico h =
              new Historico(
                  partes[0],
                  partes[1],
                  partes[2],
                  Double.parseDouble(partes[3]),
                  Double.parseDouble(partes[4]),
                  StatusAcademico.valueOf(partes[5]));
          resultado.add(h);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return resultado;
  }
}
