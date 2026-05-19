package br.edu.uepb.classroompb.view;

import java.util.Scanner;

public class TerminalCLI {
    
    // Instancia os módulos isolados
    private final AuthCLI authCLI = new AuthCLI();
    private final AdminCLI adminCLI = new AdminCLI();
    private final CoordenadorCLI coordenadorCLI = new CoordenadorCLI();
    private final ProfessorCLI professorCLI = new ProfessorCLI();
    private final AlunoCLI alunoCLI = new AlunoCLI();

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=========================================");
        System.out.println("Bem-vindo ao ClassRoomPB");
        System.out.println("Digite 'ajuda' para ver os comandos ou 'sair'.");
        System.out.println("=========================================");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("sair")) {
                System.out.println("Encerrando o ClassRoomPB...");
                break;
            }
            if (input.isEmpty()) continue;

            processarRoteamento(input);
        }
        scanner.close();
    }

    private void processarRoteamento(String input) {
        String comandoBase = input.split(" ")[0];

        // Roteamento baseado nos comandos definidos no projeto
        switch (comandoBase) {
            case "cadastrarAluno":
            case "cadastrarProfessor":
            case "cadastrarCoordenador":
            case "cadastrarAdministrador":
            case "login":
                authCLI.processar(input);
                break;
            
            case "cadastrarCurso":
            case "configurarPeriodo":
                adminCLI.processar(input);
                break;

            case "cadastrarDisciplina":
            case "ofertarTurma":
            case "editarTurma":
            case "cancelarTurma":
            case "gerarRelatorioOcupacao":
                coordenadorCLI.processar(input);
                break;

            case "registrarFrequencia":
            case "lancarNota":
                professorCLI.processar(input);
                break;

            case "solicitarMatricula":
            case "cancelarMatricula":
            case "consultarHistorico":
                alunoCLI.processar(input);
                break;

            case "ajuda":
                System.out.println("Comandos disponíveis dependem do seu perfil de usuário.");
                break;

            default:
                System.out.println("Comando não reconhecido pelo roteador central.");
        }
    }
}
