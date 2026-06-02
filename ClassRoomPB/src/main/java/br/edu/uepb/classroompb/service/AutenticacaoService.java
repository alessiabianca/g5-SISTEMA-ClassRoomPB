package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.*;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.UsuarioJaExisteException;
import br.edu.uepb.classroompb.factory.UsuarioFactory;

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

        // Validação de chaves duplicadas
        if (repository.existe(matricula)) {
            throw new UsuarioJaExisteException("Erro: Usuário com a matrícula '" + matricula + "' já está cadastrado.");
        }
        if (repository.existe(email)) { 
            throw new UsuarioJaExisteException("Erro: Usuário com o e-mail '" + email + "' já está cadastrado.");
        }

        // Refatoração da Task 1839: Delegando a fabricação da instância para a Factory
        Usuario novo = UsuarioFactory.criarUsuario(tipo, matricula, nome, email, senha);

        repository.salvar(novo);
    }

    /**
     * Task 1713: Motor de autenticação e validação de credenciais/perfis (US02)
     */
    public void realizarLogin(String id, String senha) throws Exception {
        // 1. Valida se as entradas foram preenchidas
        if (id == null || id.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
            throw new Exception("Erro: Identificador e senha devem ser preenchidos.");
        }
        
        // 2. Busca o usuário por matrícula ou e-mail na base de dados local
        Usuario usuario = repository.buscarPorId(id);
        
        // 3. Validar se a senha informada corresponde à senha armazenada para o perfil
        if (usuario == null || !usuario.getSenha().equals(senha)) {
            throw new Exception("Erro: Credenciais inválidas (Usuário não encontrado ou senha incorreta).");
        }
        
        // 4. Verificar o tipo de perfil (Aluno, Professor, Coordenador ou Administrador) para autorizar o acesso
        String perfil = usuario.getPerfil();
        if (perfil == null || perfil.trim().isEmpty()) {
            throw new Exception("Erro: Usuário não possui um perfil de acesso válido configurado.");
        }
        
        // Se todas as regras passarem, o usuário é autenticado com sucesso
        this.usuarioLogado = usuario;
        System.out.println("Acesso autorizado! Bem-vindo(a), perfil: " + perfil);
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public void realizarLogout() {
        this.usuarioLogado = null;
    }
}