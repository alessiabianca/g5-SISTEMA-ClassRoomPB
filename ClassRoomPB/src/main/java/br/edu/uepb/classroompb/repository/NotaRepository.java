package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Nota;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * US34 — Repositório de persistência para registros de notas dos alunos. Segue o padrão de arquivo
 * plano CSV já adotado pelo projeto (FrequenciaRepository, MatriculaRepository).
 */
public class NotaRepository {
  private static final String FILE_PATH = "data/notas.txt";

  public NotaRepository() {
    // Garante de forma autônoma a infraestrutura de pastas e arquivos locais
    try {
      File file = new File(FILE_PATH);
      if (file.getParentFile() != null && !file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      if (!file.exists()) {
        file.createNewFile();
      }
    } catch (IOException e) {
      System.err.println("Erro crítico ao inicializar o arquivo de notas: " + e.getMessage());
    }
  }

  /** Grava uma nota no final do arquivo utilizando a estratégia de APPEND. */
  public void salvar(Nota nota) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
      bw.write(nota.toString());
      bw.newLine();
    } catch (IOException e) {
      System.err.println("Erro ao persistir a nota em disco: " + e.getMessage());
    }
  }

  /** Recupera todas as notas salvas realizando o parsing linha a linha. */
  public List<Nota> buscarTodas() {
    List<Nota> notas = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
      String linha;
      while ((linha = br.readLine()) != null) {
        if (linha.trim().isEmpty()) continue;

        String[] partes = linha.split(";");
        if (partes.length >= 6) {
          String matriculaAluno = partes[0];
          String codigoDisciplina = partes[1];
          String periodo = partes[2];
          double nota1 = Double.parseDouble(partes[3]);
          double nota2 = Double.parseDouble(partes[4]);
          double nota3 = Double.parseDouble(partes[5]);

          notas.add(new Nota(matriculaAluno, codigoDisciplina, periodo, nota1, nota2, nota3));
        } else if (partes.length >= 5) {
          // Compatibilidade com registros de apenas 2 avaliações
          String matriculaAluno = partes[0];
          String codigoDisciplina = partes[1];
          String periodo = partes[2];
          double nota1 = Double.parseDouble(partes[3]);
          double nota2 = Double.parseDouble(partes[4]);

          notas.add(new Nota(matriculaAluno, codigoDisciplina, periodo, nota1, nota2));
        }
      }
    } catch (IOException e) {
      System.err.println("Erro ao ler o arquivo de notas: " + e.getMessage());
    }

    return notas;
  }

  /** US34: Filtra e recupera a nota de um aluno específico em uma disciplina/período. */
  public Nota buscarPorAlunoEDisciplina(
      String matriculaAluno, String codigoDisciplina, String periodo) {
    List<Nota> todas = buscarTodas();

    for (Nota n : todas) {
      if (n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && n.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && n.getPeriodo().equalsIgnoreCase(periodo)) {
        return n;
      }
    }
    return null;
  }
}
