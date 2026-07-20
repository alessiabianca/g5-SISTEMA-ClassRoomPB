package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Matricula;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MatriculaRepository {
  private static final String FILE_PATH = "data/matriculas.txt";

  public MatriculaRepository() {

    try {
      File file = new File(FILE_PATH);
      if (file.getParentFile() != null && !file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      if (!file.exists()) {
        file.createNewFile();
      }
    } catch (IOException e) {
      System.err.println("Erro crítico ao inicializar o arquivo de matrículas: " + e.getMessage());
    }
  }

  /** Recupera todas as matrículas salvas realizando o parsing linha a linha. */
  public List<Matricula> buscarTodas() {
    List<Matricula> matriculas = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
      String linha;
      while ((linha = br.readLine()) != null) {
        if (linha.trim().isEmpty()) continue;

        String[] partes = linha.split(";");
        if (partes.length >= 4) {
          String matriculaAluno = partes[0];
          String codigoDisciplina = partes[1];
          String periodo = partes[2];

          Matricula.StatusMatricula statusEnum =
              Matricula.StatusMatricula.valueOf(partes[3].toUpperCase().trim());

          matriculas.add(new Matricula(matriculaAluno, codigoDisciplina, periodo, statusEnum));
        }
      }
    } catch (IOException e) {
      System.err.println("Erro ao ler o arquivo de matrículas: " + e.getMessage());
    }

    return matriculas;
  }

  /**
   * Grava a linha CSV contendo a informação do status gerado automaticamente. Utiliza a estratégia
   * de APPEND para adicionar o registro no final do arquivo.
   */
  public void salvar(Matricula matricula) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {

      bw.write(matricula.toString());
      bw.newLine();
    } catch (IOException e) {
      System.err.println("Erro ao persistir a matrícula em disco (Task 3): " + e.getMessage());
    }
  }

  /** Reescreve o arquivo por completo mantendo o padrão CSV com os status atualizados. */
  public void atualizarArquivoCompleto(List<Matricula> matriculasAtualizadas) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
      for (Matricula m : matriculasAtualizadas) {
        bw.write(m.toString());
        bw.newLine();
      }
    } catch (IOException e) {
      System.err.println("Erro ao reescrever o arquivo de matrículas (Task 3): " + e.getMessage());
    }
  }
}
