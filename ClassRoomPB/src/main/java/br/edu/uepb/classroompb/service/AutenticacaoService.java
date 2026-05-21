package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.*;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.UsuarioJaExisteException;

public class AutenticacaoService {
    private static AutenticacaoService instancia;
    private final UsuarioRepository repository;
    private Usuario usuarioLogado; 

    private AutenticacaoService() {
        this.repository = new UsuarioRepository();
    }

    public static AutenticacaoService getInstancia() {
        if (instancia == null) {
            instancia = new AutenticacaoService();
        }
        return instancia;
    }

    public void cadastrarUsuario(String tipo, String nome, String matricula, String email, String senha) throws Exception {
        if (tipo == null || tipo.trim().isEmpty() ||
            nome == null || nome.trim().isEmpty() ||
            matricula == null || matricula.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            senha == null || senha.trim().isEmpty()) {
            throw new Exception("Erro: Todos os campos são obrigatórios e não podem estar em branco.");
        }

        // Lançando as exceções personalizadas conforme a Task 1838
        if (repository.existe(matricula)) {
            throw new UsuarioJaExisteException("Erro: Usuário com a matrícula '" + matricula + "' já está cadastrado.");
        }
        if (repository.existe(email)) { 
            throw new UsuarioJaExisteException("Erro: Usuário com o e-mail '" + email + "' já está cadastrado.");
        }

        Usuario novo = switch (tipo.toLowerCase()) {
            case "aluno" -> new Aluno(matricula, nome, email, senha);
            case "professor" -> new Professor(matricula, nome, email, senha);
            case "coordenador" -> new Coordenador(matricula, nome, email, senha);
            case "administrador" -> new Administrador(matricula, nome, email, senha);
            default -> throw new Exception("Erro: Tipo de perfil '" + tipo + "' desconhecido.");
        };

        repository.salvar(novo);
    }

    public void realizarLogin(String id, String senha) throws Exception {
        if (id == null || id.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
            throw new Exception("Erro: Identificador e senha devem ser preenchidos.");
        }
        
        Usuario usuario = repository.buscarPorId(id);
        if (usuario == null || !usuario.getSenha().equals(senha)) {
            throw new Exception("Erro: Credenciais inválidas.");
        }
        this.usuarioLogado = usuario;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public void realizarLogout() {
        this.usuarioLogado = null;
    }
}