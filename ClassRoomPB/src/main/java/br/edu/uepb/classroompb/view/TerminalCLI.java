package br.edu.uepb.classroompb.view;

import java.util.Scanner;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.PeriodoService; 
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.service.TurmaService;

public class TerminalCLI {
    private final AuthCLI authCLI = new AuthCLI();
    private final CoordenadorCLI coordenadorCLI = new CoordenadorCLI();
    private final ProfessorCLI professorCLI = new ProfessorCLI();
    private final AlunoCLI alunoCLI = new AlunoCLI();

    PeriodoRepository periodoRepository = new PeriodoRepository();
    TurmaRepository turmaRepository = new TurmaRepository();
    DisciplinaRepository disciplinaRepository = new DisciplinaRepository();

    PeriodoService periodoService = new PeriodoService(periodoRepository);
    TurmaService turmaService = new TurmaService(turmaRepository, periodoRepository, disciplinaRepository);

    AdminCLI adminCLI = new AdminCLI(periodoService, turmaService);

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=========================================");
        System.out.println("ClassRoomPB CLI - Modo Roteador Inicial");
        System.out.println("=========================================");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("sair")) {
                System.out.println("Encerrando o sistema...");
                break;
            }
            if (input.isEmpty()) continue;

            processarRoteamento(input);
        }
        scanner.close();
    }

    private void processarRoteamento(String input) {
        String comandoBase = input.split(" ")[0];

        try {
            switch (comandoBase) {
                case "login":
                case "cadastrarAluno":
                case "cadastrarProfessor":
                case "cadastrarCoordenador":
                case "cadastrarAdministrador":
                    authCLI.processar(input);
                    break;
                
                case "cadastrarCurso":
                case "configurarPeriodo":
                case "cadastrarPeriodo":
                case "ativarPeriodo":
                case "encerrarPeriodo":
                    adminCLI.processar(input);
                    break;

                case "cadastrarDisciplina":
                case "ofertarTurma":
                case "editarTurma":
                case "cancelarTurma":
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

                default:
                    System.out.println("Comando não reconhecido ou funcionalidade ainda não implementada.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar o comando: " + e.getMessage());
        }
    }
}