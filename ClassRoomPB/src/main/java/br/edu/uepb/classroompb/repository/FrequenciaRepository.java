package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Frequencia;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FrequenciaRepository {
    private static final String FILE_PATH = "data/frequencias.txt";

    public FrequenciaRepository() {
        try {
            File file = new File(FILE_PATH);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Erro crítico ao inicializar o diário de frequências: " + e.getMessage());
        }
    }

    
     // Adiciona em lote uma lista de frequências no final do arquivo plano.
     
    public void salvarLote(List<Frequencia> frequencias) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            for (Frequencia f : frequencias) {
                bw.write(f.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao persistir frequências em lote: " + e.getMessage());
        }
    }

    
    //  Recupera todo o histórico gravado em disco.
     
    public List<Frequencia> buscarTodas() {
        List<Frequencia> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] partes = linha.split(";");
                if (partes.length >= 5) {
                    Frequencia.TipoFrequencia statusEnum = Frequencia.TipoFrequencia.valueOf(partes[4].toUpperCase().trim());
                    lista.add(new Frequencia(partes[0], partes[1], partes[2], partes[3], statusEnum));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler histórico de frequências: " + e.getMessage());
        }
        return lista;
    }

     // US28: Filtra e recupera todos os registros de frequência de um aluno específico em uma turma.
     
    public List<Frequencia> buscarPorAlunoEDisciplina(String matriculaAluno, String codigoDisciplina, String periodo) {
        List<Frequencia> filtradas = new ArrayList<>();
        List<Frequencia> todas = buscarTodas();

        for (Frequencia f : todas) {
            if (f.getMatriculaAluno().equalsIgnoreCase(matriculaAluno) &&
                f.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) &&
                f.getPeriodo().equalsIgnoreCase(periodo)) {
                filtradas.add(f);
            }
        }
        return filtradas;
    }
}