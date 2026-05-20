package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Curso;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CursoRepository {
    private static final String FILE_PATH = "data/cursos.txt";

    public CursoRepository() {
        // Garante a existência do diretório e do arquivo
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Erro ao inicializar o arquivo de armazenamento de cursos.", e);
            }
        }
    }

    public void salvar(Curso curso) throws IOException {
        // Uso estrito de FileWriter com append = true
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(curso.toString());
            writer.newLine();
        }
    }

    public List<Curso> listarTodos() throws IOException {
        List<Curso> cursos = new ArrayList<>();
        // Leitura linha por linha e split por ";"
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";");
                if (dados.length >= 2) {
                    Curso curso = new Curso(dados[0], dados[1]);
                    cursos.add(curso);
                }
            }
        }
        return cursos;
    }

    public Curso buscarPorCodigo(String codigo) throws IOException {
        List<Curso> cursos = listarTodos();
        for (Curso curso : cursos) {
            if (curso.getCodigo().equalsIgnoreCase(codigo)) {
                return curso;
            }
        }
        return null;
    }
}