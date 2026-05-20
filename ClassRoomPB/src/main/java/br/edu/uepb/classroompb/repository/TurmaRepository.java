package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Turma;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TurmaRepository {
    private static final String ARQUIVO = "data/turmas.txt";

    public void salvar(Turma turma) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, true))) {
            bw.write(turma.toString());
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar a turma no ficheiro local.", e);
        }
    }

    public List<Turma> buscarTodas() {
        List<Turma> turmas = new ArrayList<>();
        File file = new File(ARQUIVO);
        
        if (!file.exists()) {
            return turmas;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ARQUIVO))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length == 6) {
                    Turma t = new Turma(dados[0], dados[1], dados[2], Integer.parseInt(dados[3]), dados[4], dados[5]);
                    turmas.add(t);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o ficheiro de turmas: " + e.getMessage());
        }
        return turmas;
    }

    public void atualizarArquivoCompleto(List<Turma> turmasAtualizadas) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, false))) { // false = sobrescrever
            for (Turma t : turmasAtualizadas) {
                bw.write(t.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao atualizar o base de dados local.", e);
        }
    }
}