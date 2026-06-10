// src/main/java/br/edu/uepb/classroompb/repository/MatriculaRepository.java
package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Matricula;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MatriculaRepository {
    private static final String FILE_PATH = "data/matriculas.txt";

    public MatriculaRepository() {
        // Garante a existência do diretório e do arquivo local de persistência
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

    /**
     * Recupera todas as matrículas salvas no arquivo plano (.txt) realizando o parsing linha a linha.
     */
    public List<Matricula> buscarTodas() {
        List<Matricula> matriculas = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                
                // Garantia de leitura linha a linha separada por ponto e vírgula
                String[] partes = linha.split(";");
                if (partes.length >= 4) {
                    String matriculaAluno = partes[0];
                    String codigoDisciplina = partes[1];
                    String periodo = partes[2];
                    String status = partes[3];
                    
                    matriculas.add(new Matricula(matriculaAluno, codigoDisciplina, periodo, status));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo de matrículas: " + e.getMessage());
        }
        
        return matriculas;
    }

    /**
     * Salva uma nova matrícula no arquivo texto utilizando a estratégia de APPEND (sem sobrescrever).
     */
    public void salvar(Matricula matricula) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(matricula.toString());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao persistir a matrícula: " + e.getMessage());
        }
    }

    /**
     * Reescreve o arquivo por completo. Útil quando uma matrícula muda de status (US16).
     */
    public void atualizarArquivoCompleto(List<Matricula> matriculasAtualizadas) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Matricula m : matriculasAtualizadas) {
                bw.write(m.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao atualizar o arquivo completo de matrículas: " + e.getMessage());
        }
    }
}