package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Turma;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TurmaRepository {
    
    private static final String ARQUIVO = "data/turmas.txt";

    // Busca todas as turmas salvas no arquivo
    public List<Turma> buscarTodas() {
        List<Turma> turmas = new ArrayList<>();
        File file = new File(ARQUIVO);
        
        if (!file.exists()) return turmas;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                
                String[] dados = linha.split(";");
                
                // AJUSTE DE SUPORTE PARA 7 COLUNAS (USO DE VAGAS OCUPADAS)
                if (dados.length == 7) {
                    turmas.add(new Turma(
                        dados[0], // codigoDisciplina
                        dados[1], // matriculaProfessor
                        dados[2], // periodo
                        Integer.parseInt(dados[3]), // vagas
                        Integer.parseInt(dados[4]), // vagasOcupadas
                        dados[5], // horario
                        dados[6]  // sala
                    ));
                } 
                // Mantém compatibilidade com linhas antigas de 6 colunas, se houver
                else if (dados.length == 6) {
                    turmas.add(new Turma(dados[0], dados[1], dados[2], Integer.parseInt(dados[3]), dados[4], dados[5]));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler turmas: " + e.getMessage());
        }
        return turmas;
    }

    // Salva uma nova turma adicionando uma linha no final do arquivo (append)
    public void salvar(Turma turma) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO, true))) {
            bw.write(turma.toString());
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar turma.", e);
        }
    }

    // Sobrescreve todo o arquivo. Usado apenas para exclusão e edição (US14).
    public void atualizarArquivoCompleto(List<Turma> turmasAtualizadas) {
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