package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Turma;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TurmaRepository {

  private static final String DIRECTORY = "data";
  private static final String ARQUIVO = "data/turmas.txt";

  public TurmaRepository() {
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
      System.err.println("Erro ao inicializar arquivo de turmas: " + e.getMessage());
    }
  }

  public List<Turma> buscarTodas() {
    List<Turma> turmas = new ArrayList<>();
    File file = new File(ARQUIVO);

    if (!file.exists()) return turmas;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String linha;
      while ((linha = br.readLine()) != null) {
        if (linha.trim().isEmpty()) continue;

        String[] dados = linha.split(";");

        if (dados.length >= 4) {
          turmas.add(
              new Turma(
                  dados[0], dados[1], Integer.parseInt(dados[2]), Integer.parseInt(dados[3])));
        } else if (dados.length == 3) {
          turmas.add(new Turma(dados[0], dados[1], Integer.parseInt(dados[2])));
        }
      }
    } catch (IOException e) {
      System.err.println("Erro ao ler turmas: " + e.getMessage());
    }
    return turmas;
  }

  public void salvar(Turma turma) {
    garantirDiretorioEArquivo();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, true))) {
      bw.write(turma.toString());
      bw.newLine();
    } catch (IOException e) {
      throw new RuntimeException("Erro ao salvar turma.", e);
    }
  }

  public void atualizarArquivoCompleto(List<Turma> turmasAtualizadas) {
    garantirDiretorioEArquivo();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, false))) {
      for (Turma t : turmasAtualizadas) {
        bw.write(t.toString());
        bw.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException("Erro ao atualizar arquivo.", e);
    }
  }
}
