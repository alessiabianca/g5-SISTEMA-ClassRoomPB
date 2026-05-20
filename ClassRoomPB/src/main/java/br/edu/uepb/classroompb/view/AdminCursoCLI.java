package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.service.CursoService;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.IOException;
import java.util.Scanner;

public class AdminCursoCLI {
    private final CursoService cursoService;
    private final Scanner scanner;

    public AdminCursoCLI(CursoService cursoService) {
        this.cursoService = cursoService;
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenu(String papelUsuarioLogado) {
        // Roteamento interno da Sub-CLI de Cursos
        while (true) {
            System.out.println("\n=== GERENCIAMENTO DE CURSOS (ADMIN) ===");
            System.out.println("1. Cadastrar Novo Curso");
            System.out.println("0. Voltar ao Menu Anterior");
            System.out.print("Escolha uma opção: ");
            
            String opcao = scanner.nextLine().trim();
            
            if (opcao.equals("1")) {
                executarCadastro(papelUsuarioLogado);
            } else if (opcao.equals("0")) {
                break;
            } else {
                System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private void executarCadastro(String papelUsuarioLogado) {
        System.out.println("\n--- CADASTRO DE CURSO ---");
        System.out.print("Digite o código do curso: ");
        String codigo = scanner.nextLine();
        
        System.out.print("Digite o nome do curso: ");
        String nome = scanner.nextLine();

        try {
            cursoService.cadastrarCurso(codigo, nome, papelUsuarioLogado);
            // Critério de Aceitação: Mensagem de sucesso
            System.out.println("Sucesso: Curso cadastrado perfeitamente.");
        } catch (ValidacaoException e) {
            // Critério de Aceitação: Mensagem de erro limpa via System.err
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println("Erro interno ao salvar os dados no arquivo local: " + e.getMessage());
        }
    }
}