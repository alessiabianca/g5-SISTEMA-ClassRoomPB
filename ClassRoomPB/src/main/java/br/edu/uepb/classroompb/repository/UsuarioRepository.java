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

    // Modificado para buscar de forma abrangente por matrícula ou e-mail nos valores salvos
    public boolean existe(String identificador) {
        if (identificador == null) return false;
        
        // Verifica primeiro se a chave direta (matrícula/id usado no salvamento) existe
        if (dados.containsKey(identificador)) {
            return true;
        }
        
        // Varre todos os valores para garantir o bloqueio caso o e-mail ou a matrícula coincidam
        for (Usuario u : dados.values()) {
            if (identificador.equalsIgnoreCase(u.getMatricula()) || identificador.equalsIgnoreCase(u.getEmail())) {
                return true;
            }
        }
        return false;
    }

    public void salvar(Usuario usuario) {
        if (usuario == null) return;
        
        // Mantém a lógica original de decidir qual chave prioritária usar no put
        String chave = (usuario.getMatricula() != null && !usuario.getMatricula().isEmpty()) 
                       ? usuario.getMatricula() 
                       : usuario.getEmail();
                       
        if (chave != null && !chave.isEmpty()) {
            dados.put(chave, usuario);
            sincronizarComDisco();
        }
    }

    // Modificado para recuperar o usuário permitindo o login por matrícula ou por e-mail
    public Usuario buscarPorId(String identificador) {
        if (identificador == null) return null;
        
        // Se for a chave direta do mapa, retorna de imediato
        if (dados.containsKey(identificador)) {
            return dados.get(identificador);
        }
        
        // Caso o usuário tente logar usando o e-mail, varre os objetos para encontrá-lo
        for (Usuario u : dados.values()) {
            if (identificador.equalsIgnoreCase(u.getMatricula()) || identificador.equalsIgnoreCase(u.getEmail())) {
                return u;
            }
        }
        return null;
    }

    private void sincronizarComDisco() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(dados);
        } catch (IOException e) {
            System.err.println("Erro crítico: Falha de escrita ao salvar os dados dos usuários no disco. Erro: " + e.getMessage());
        }
    }
}