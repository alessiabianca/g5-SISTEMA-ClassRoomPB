package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Aula;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AulaRepository {

  private static final String DIRECTORY = "data";
  private static final String ARQUIVO = "data/aulas.txt";

  public AulaRepository() {
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
      System.err.println("Erro ao inicializar arquivo de aulas: " + e.getMessage());
    }
  }

  public List<Aula> buscarTodas() {
    List<Aula> aulas = new ArrayList<>();
    File file = new File(ARQUIVO);

    if (!file.exists()) return aulas;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String linha;
      while ((linha = br.readLine()) != null) {
        if (linha.trim().isEmpty()) continue;

        String[] dados = linha.split(";");
        if (dados.length >= 5) {
          Aula aula =
              new Aula(
                  dados[0],
                  dados[1],
                  dados[2],
                  dados[3],
                  Integer.parseInt(dados[4]));
          aulas.add(aula);
        }
      }
    } catch (IOException e) {
      System.err.println("Erro ao ler aulas: " + e.getMessage());
    }
    return aulas;
  }

  public List<Aula> buscarPorDiario(String codigoDiario) {
    List<Aula> resultado = new ArrayList<>();
    for (Aula a : buscarTodas()) {
      if (a.getCodigoDiario().equalsIgnoreCase(codigoDiario)) {
        resultado.add(a);
      }
    }
    return resultado;
  }

  public void salvar(Aula aula) {
    garantirDiretorioEArquivo();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, true))) {
      bw.write(aula.toString());
      bw.newLine();
    } catch (IOException e) {
      throw new RuntimeException("Erro ao salvar aula.", e);
    }
  }

  public void atualizarArquivoCompleto(List<Aula> aulasAtualizadas) {
    garantirDiretorioEArquivo();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, false))) {
      for (Aula a : aulasAtualizadas) {
        bw.write(a.toString());
        bw.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException("Erro ao atualizar arquivo de aulas.", e);
    }
  }
}