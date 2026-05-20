package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Periodo;
import java.io.*;

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
}