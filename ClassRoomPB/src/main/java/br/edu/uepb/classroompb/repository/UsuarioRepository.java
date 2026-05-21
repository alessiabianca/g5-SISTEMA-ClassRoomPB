package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Usuario;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UsuarioRepository {
    private static final String FILE_NAME = "usuarios.dat";
    private Map<String, Usuario> dados;

    @SuppressWarnings("unchecked")
    public UsuarioRepository() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                this.dados = (Map<String, Usuario>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Aviso: Falha ao carregar dados do disco. Inicializando base vazia. Erro: " + e.getMessage());
                this.dados = new HashMap<>();
            }
        } else {
            this.dados = new HashMap<>();
        }
    }

    public boolean existe(String identificador) {
        if (identificador == null) return false;
        return dados.containsKey(identificador);
    }

    public void salvar(Usuario usuario) {
        if (usuario == null) return;
        
        // Verifica se foi fornecida a matrícula ou o e-mail como chave primária única
        String chave = (usuario.getMatricula() != null && !usuario.getMatricula().isEmpty()) 
                       ? usuario.getMatricula() 
                       : usuario.getEmail();
                       
        if (chave != null && !chave.isEmpty()) {
            dados.put(chave, usuario);
            sincronizarComDisco();
        }
    }

    public Usuario buscarPorId(String identificador) {
        if (identificador == null) return null;
        return dados.get(identificador);
    }

    private void sincronizarComDisco() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(dados);
        } catch (IOException e) {
            System.err.println("Erro crítico: Falha de escrita ao salvar os dados dos usuários no disco. Erro: " + e.getMessage());
        }
    }
}