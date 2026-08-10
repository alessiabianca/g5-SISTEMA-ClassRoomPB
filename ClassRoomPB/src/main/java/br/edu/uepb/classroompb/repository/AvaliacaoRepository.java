package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Avaliacao;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoRepository implements Repository<br.edu.uepb.classroompb.model.Avaliacao> {

  private static final String DIRECTORY = "data";
  private static final String ARQUIVO = "data/avaliacoes.txt";

  public AvaliacaoRepository() {
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
      System.err.println("Erro ao inicializar arquivo de avaliacoes: " + e.getMessage());
    }
  }

  public List<Avaliacao> buscarTodas() {
    List<Avaliacao> avaliacoes = new ArrayList<>();
    File file = new File(ARQUIVO);

    if (!file.exists()) return avaliacoes;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String linha;
      while ((linha = br.readLine()) != null) {
        if (linha.trim().isEmpty()) continue;

        String[] dados = linha.split(";");
        if (dados.length >= 6) {
          Avaliacao avaliacao =
              new Avaliacao(
                  dados[0],
                  dados[1],
                  dados[2],
                  Integer.parseInt(dados[3]),
                  Double.parseDouble(dados[4]),
                  Double.parseDouble(dados[5]));
          avaliacoes.add(avaliacao);
        }
      }
    } catch (IOException e) {
      System.err.println("Erro ao ler avaliacoes: " + e.getMessage());
    }
    return avaliacoes;
  }

  public List<Avaliacao> buscarPorDiario(String codigoDiario) {
    List<Avaliacao> resultado = new ArrayList<>();
    for (Avaliacao av : buscarTodas()) {
      if (av.getCodigoDiario().equalsIgnoreCase(codigoDiario)) {
        resultado.add(av);
      }
    }
    return resultado;
  }

  public void salvar(Avaliacao avaliacao) {
    garantirDiretorioEArquivo();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, true))) {
      bw.write(avaliacao.toString());
      bw.newLine();
    } catch (IOException e) {
      throw new RuntimeException("Erro ao salvar avaliacao.", e);
    }
  }

  public void atualizarArquivoCompleto(List<Avaliacao> avaliacoesAtualizadas) {
    garantirDiretorioEArquivo();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, false))) {
      for (Avaliacao av : avaliacoesAtualizadas) {
        bw.write(av.toString());
        bw.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException("Erro ao atualizar arquivo de avaliacoes.", e);
    }
  }
}
