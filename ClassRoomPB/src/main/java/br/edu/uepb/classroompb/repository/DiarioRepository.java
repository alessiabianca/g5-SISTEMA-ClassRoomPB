package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Diario;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DiarioRepository {

  private static final String DIRECTORY = "data";
  private static final String ARQUIVO = "data/diarios.txt";

  public DiarioRepository() {
    garantirDiretorioEArquivo();
  }

  private void garantirDiretorioEArquivo() {
    try {
      File dir = new File(DIRECTORY);
      if (!dir.exists()) {
        dir.mkdirs();
      }
      File file = new File(ARQUIVO);
      if (!file.exists()) {
        file.createNewFile();
      }
    } catch (IOException e) {
      System.err.println("Erro ao inicializar arquivo de diarios: " + e.getMessage());
    }
  }

  public List<Diario> buscarTodos() {
    List<Diario> diarios = new ArrayList<>();
    File file = new File(ARQUIVO);

    if (!file.exists()) return diarios;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String linha;
      while ((linha = br.readLine()) != null) {
        if (linha.trim().isEmpty()) continue;

        String[] dados = linha.split(";");
        if (dados.length >= 9) {
          Diario diario =
              new Diario(
                  dados[0],
                  dados[1],
                  dados[2],
                  dados[3],
                  dados[4],
                  dados[5],
                  dados[6],
                  Integer.parseInt(dados[7]),
                  Diario.SituacaoDiario.valueOf(dados[8]));
          diarios.add(diario);
        }
      }
    } catch (IOException e) {
      System.err.println("Erro ao ler diarios: " + e.getMessage());
    }
    return diarios;
  }

  public Diario buscarPorCodigo(String codigo) {
    for (Diario d : buscarTodos()) {
      if (d.getCodigo().equalsIgnoreCase(codigo)) {
        return d;
      }
    }
    return null;
  }

  public void salvar(Diario diario) {
    garantirDiretorioEArquivo();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, true))) {
      bw.write(diario.toString());
      bw.newLine();
    } catch (IOException e) {
      throw new RuntimeException("Erro ao salvar diario.", e);
    }
  }

  public void atualizarArquivoCompleto(List<Diario> diariosAtualizados) {
    garantirDiretorioEArquivo();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, false))) {
      for (Diario d : diariosAtualizados) {
        bw.write(d.toString());
        bw.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException("Erro ao atualizar arquivo de diarios.", e);
    }
  }
}
