// src/main/java/br/edu/uepb/classroompb/repository/PeriodoRepository.java
package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Periodo;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PeriodoRepository {
    private static final String ARQUIVO = "data/periodos.txt";

    public Periodo buscarPorCodigo(String codigo) {
        File file = new File(ARQUIVO);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(ARQUIVO))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length >= 2 && dados[0].equalsIgnoreCase(codigo)) {
                    return new Periodo(dados[0], dados[1]);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler periodos: " + e.getMessage());
        }
        return null;
    }

    // --- ADICIONE OS MÉTODOS ABAIXO MANTENDO O PADRÃO DA TASK 1833 ---

    public void salvar(Periodo periodo) throws IOException {
        // Uso estrito de FileWriter com append = true conforme especificado pelo gestor
        File file = new File(ARQUIVO);
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            file.createNewFile();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO, true))) {
            writer.write(periodo.toString());
            writer.newLine();
        }
    }

    public void atualizarTodos(List<Periodo> periodos) throws IOException {
        // Necessário para a troca de estados (PLANEJADO -> INICIADO -> ENCERRADO)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO, false))) {
            for (Periodo periodo : periodos) {
                writer.write(periodo.toString());
                writer.newLine();
            }
        }
    }

    public List<Periodo> listarTodos() throws IOException {
        List<Periodo> periodos = new ArrayList<>();
        File file = new File(ARQUIVO);
        if (!file.exists()) return periodos;

        try (BufferedReader reader = new BufferedReader(new FileReader(ARQUIVO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";");
                if (dados.length >= 2) {
                    periodos.add(new Periodo(dados[0], dados[1]));
                }
            }
        }
        return periodos;
    }
}