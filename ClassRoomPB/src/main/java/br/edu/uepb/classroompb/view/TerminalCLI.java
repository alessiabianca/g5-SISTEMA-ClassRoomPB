package br.edu.uepb.classroompb.view;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.CursoRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.FrequenciaRepository; 
import br.edu.uepb.classroompb.service.PeriodoService; 
import br.edu.uepb.classroompb.service.DisciplinaService;
import br.edu.uepb.classroompb.service.CursoService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.MatriculaService;
import br.edu.uepb.classroompb.service.NotaService;
import br.edu.uepb.classroompb.service.FrequenciaService; 
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.model.Usuario;

public class TerminalCLI {
    private final AuthCLI authCLI = new AuthCLI();
    private final CoordenadorCLI coordenadorCLI = new CoordenadorCLI();

    private final PeriodoRepository periodoRepository = new PeriodoRepository();
    private final DisciplinaRepository disciplinaRepository = new DisciplinaRepository();
    private final TurmaRepository turretRepository = new TurmaRepository(); 
    private final CursoRepository cursoRepository = new CursoRepository();
    private final MatriculaRepository matriculaRepository = new MatriculaRepository();
    private final FrequenciaRepository frequenciaRepository = new FrequenciaRepository(); 

    private final PeriodoService periodoService = new PeriodoService(periodoRepository);
    private final DisciplinaService disciplinaService = new DisciplinaService(disciplinaRepository);
    private final CursoService cursoService = new CursoService(cursoRepository);
    private final TurmaService turmaService = new TurmaService(turretRepository, periodoRepository, disciplinaRepository);
    private final MatriculaService matriculaService = new MatriculaService(turretRepository, matriculaRepository, periodoRepository);

    private final NotaRepository notaRepository = new NotaRepository();
    private final NotaService notaService = new NotaService(notaRepository, turretRepository, matriculaRepository, periodoRepository); // INSTANCIADO (US35: + periodoRepository)

    private final FrequenciaService frequenciaService = new FrequenciaService(turretRepository, matriculaRepository, frequenciaRepository, notaRepository); // INSTANCIADO (US27)
    private final AutenticacaoService authService = AutenticacaoService.getInstancia();


    private final AlunoCLI alunoCLI = new AlunoCLI(turmaService, matriculaService);
    private final ProfessorCLI professorCLI = new ProfessorCLI(turmaService, frequenciaService, notaService); // INSTANCIADO (US27)

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            Usuario logado = authService.getUsuarioLogado();
            
            System.out.println("\n=========================================");
            System.out.println("            ClassRoomPB CLI              ");
            if (logado != null) {
                System.out.println(" Usuário: " + logado.getNome() + " [" + logado.getPerfil() + "]");
            } else {
                System.out.println(" Status: Modo Visitante (Não Autenticado)");
            }
            System.out.println("=========================================");

            if (logado == null) {
                System.out.println("1. Realizar Login");
                System.out.println("2. Cadastrar Aluno");
                System.out.println("3. Cadastrar Professor");
                System.out.println("4. Cadastrar Coordenador");
                System.out.println("5. Cadastrar Administrador");
                System.out.println("0. Sair do Sistema");
                System.out.println("=========================================");
                System.out.print("Escolha uma opção: ");
                
                String opcao = scanner.nextLine().trim();
                if (opcao.equals("0")) {
                    System.out.println("Encerrando o system...");
                    break;
                }
                processarMenuVisitante(opcao, scanner);

            } else {
                String perfil = logado.getPerfil().toUpperCase();
                switch (perfil) {
                    case "ADMINISTRADOR":
                        exibirMenuAdmin(scanner, logado.getPerfil());
                        break;
                    case "COORDENADOR":
                        exibirMenuCoordenador(scanner, logado.getPerfil());
                        break;
                    case "ALUNO":
                        exibirMenuAluno(scanner, logado.getMatricula());
                        break;
                    case "PROFESSOR": // ROTEAMENTO ADICIONADO (US27)
                        exibirMenuProfessor(scanner, logado.getMatricula());
                        break;
                    default:
                        System.out.println("Área do " + perfil + " em desenvolvimento.");
                        System.out.println("1. Fazer Logout (Encerrar Sessão)");
                        System.out.print("Escolha uma opção: ");
                        if (scanner.nextLine().trim().equals("1")) {
                            authCLI.processar("logout");
                        }
                        break;
                }
            }
        }
        scanner.close();
    }

    private void processarMenuVisitante(String opcao, Scanner scanner) {
        switch (opcao) {
            case "1":
                System.out.print("Digite o Identificador (E-mail/Matrícula): ");
                String id = scanner.nextLine().trim();
                System.out.print("Digite a Senha: ");
                String senha = scanner.nextLine().trim();
                authCLI.processar("login " + id + " " + senha);
                break;

            case "2": case "3": case "4": case "5":
                String comando = "";
                if (opcao.equals("2")) comando = "cadastrarAluno";
                if (opcao.equals("3")) comando = "cadastrarProfessor";
                if (opcao.equals("4")) comando = "cadastrarCoordenador";
                if (opcao.equals("5")) comando = "cadastrarAdministrador";

                System.out.print("Nome Completo (Use_Underlines_No_Lugar_De_Espacos): ");
                String nome = scanner.nextLine().trim();
                System.out.print("CPF: ");
                String cpf = scanner.nextLine().trim();
                System.out.print("Matrícula: ");
                String mat = scanner.nextLine().trim();
                System.out.print("E-mail: ");
                String email = scanner.nextLine().trim();
                System.out.print("Senha: ");
                String pass = scanner.nextLine().trim();

                authCLI.processar(comando + " " + nome + " " + cpf + " " + mat + " " + email + " " + pass);
                break;

            default:
                System.out.println("Opção inválida!");
        }
    }

    private void exibirMenuAdmin(Scanner scanner, String perfilLogado) {
        System.out.println("1. Cadastrar Período Letivo");
        System.out.println("2. Ativar/Iniciar Período Letivo");
        System.out.println("3. Encerrar Período Letivo");
        System.out.println("4. Cadastrar Novo Curso");
        System.out.println("5. Fazer Logout (Encerrar Sessão)");
        System.out.println("=========================================");
        System.out.print("Escolha uma opção: ");
        String op = scanner.nextLine().trim();

        try {
            switch (op) {
                case "1":
                    System.out.print("Digite o código do período (ex: 2026.1): ");
                    String p1 = scanner.nextLine().trim();
                    periodoService.cadastrarPeriodo(p1);
                    System.out.println("Sucesso: Período '" + p1 + "' cadastrado com status PLANEJADO.");
                    break;
                case "2":
                    System.out.print("Digite o código do período a ser ativado: ");
                    String p2 = scanner.nextLine().trim();
                    periodoService.activarPeriodo(p2);
                    System.out.println("Sucesso: Período '" + p2 + "' ativado (INICIADO) com sucesso.");
                    break;
                case "3":
                    System.out.print("Digite o código do período a ser encerrado: ");
                    String p3 = scanner.nextLine().trim();
                    periodoService.encerrarPeriodo(p3);
                    System.out.println("Sucesso: Período '" + p3 + "' alterado para ENCERRADO.");
                    break;
                case "4":
                    System.out.print("Código do Curso (ex: CC): ");
                    String codC = scanner.nextLine().trim();
                    System.out.print("Nome Completo do Curso: ");
                    String nomeC = scanner.nextLine().trim();
                    cursoService.cadastrarCurso(codC, nomeC, perfilLogado);
                    System.out.println("Sucesso: Curso '" + nomeC + "' registrado na base de dados.");
                    break;
                case "5":
                    authCLI.processar("logout");
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        } catch (Exception e) {
            System.err.println("ERRO DE NEGÓCIO: " + e.getMessage());
        }
    }

    private void exibirMenuCoordenador(Scanner scanner, String perfilLogado) {
        System.out.println("1. Cadastrar Nova Disciplina");
        System.out.println("2. Ofertar Nova Turma");
        System.out.println("3. Editar Turma Existente");
        System.out.println("4. Cancelar Oferta de Turma");
        System.out.println("5. Visualizar Lista de Espera de Turma [US26]");
        System.out.println("6. Fazer Logout (Encerrar Sessão)");
        System.out.println("=========================================");
        System.out.print("Escolha uma opção: ");
        String op = scanner.nextLine().trim();

        try {
            switch (op) {
                case "1":
                    System.out.print("Código da Disciplina (ex: P1): ");
                    String codD = scanner.nextLine().trim();
                    System.out.print("Nome da Disciplina: ");
                    String nomeD = scanner.nextLine().trim();
                    System.out.print("Carga Horária (inteiro positivo): ");
                    int ch = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Créditos (inteiro positivo): ");
                    int cred = Integer.parseInt(scanner.nextLine().trim());
                    
                    List<String> preReqs = new ArrayList<>();
                    System.out.print("Possui código de pré-requisito? (Deixe em branco se não): ");
                    String pr = scanner.nextLine().trim();
                    if (!pr.isEmpty()) preReqs.add(pr);

                    disciplinaService.cadastrarDisciplina(codD, nomeD, ch, cred, preReqs, perfilLogado);
                    System.out.println("Sucesso: Disciplina '" + nomeD + "' cadastrada com êxito!");
                    break;

                case "2":
                    System.out.print("ID da Disciplina: ");
                    String idDisc = scanner.nextLine().trim();
                    System.out.print("ID/Matrícula do Professor: ");
                    String idProf = scanner.nextLine().trim();
                    System.out.print("Período Letivo (ex: 2026.1): ");
                    String per = scanner.nextLine().trim();
                    System.out.print("Limite de Vagas: ");
                    int vagas = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Horário (ex: 08:00-10:00): ");
                    String hor = scanner.nextLine().trim();
                    System.out.print("Sala (ex: Sala_B3): ");
                    String sala = scanner.nextLine().trim();

                    coordenadorCLI.processar("ofertarTurma " + idDisc + " " + idProf + " " + per + " " + vagas + " " + hor + " " + sala);
                    break;

                case "3":
                    System.out.print("Código da Disciplina da Turma: ");
                    String dEdit = scanner.nextLine().trim();
                    System.out.print("Período da Turma: ");
                    String pEdit = scanner.nextLine().trim();
                    System.out.print("ID/Matrícula do Novo Professor: ");
                    String profEdit = scanner.nextLine().trim();
                    System.out.print("Novo Limite de Vagas: ");
                    String vEdit = scanner.nextLine().trim();
                    System.out.print("Novo Horário: ");
                    String hEdit = scanner.nextLine().trim();
                    System.out.print("Nova Sala: ");
                    String sEdit = scanner.nextLine().trim();

                    coordenadorCLI.processar("editarTurma " + dEdit + " " + pEdit + " " + profEdit + " " + vEdit + " " + hEdit + " " + sEdit);
                    break;

                case "4":
                    System.out.print("Código da Disciplina da Turma: ");
                    String dCanc = scanner.nextLine().trim();
                    System.out.print("Período da Turma: ");
                    String pCanc = scanner.nextLine().trim();
                    coordenadorCLI.processar("cancelarTurma " + dCanc + " " + pCanc);
                    break;

                case "5": 
                    System.out.print("Código da Disciplina: ");
                    String cEspera = scanner.nextLine().trim();
                    System.out.print("Período Letivo da Turma: ");
                    String pEspera = scanner.nextLine().trim();
                    
                    coordenadorCLI.processar("exibirListaEspera " + cEspera + " " + pEspera);
                    break;

                case "6":
                    authCLI.processar("logout");
                    break;

                default:
                    System.out.println("Opção inválida!");
            }
        } catch (NumberFormatException e) {
            System.err.println("ERRO DE FORMATO: Valores numéricos de carga, créditos ou vagas inválidos.");
        } catch (Exception e) {
            System.err.println("ERRO INTERNO: " + e.getMessage());
        }
    }

    private void exibirMenuAluno(Scanner scanner, String matriculaLogada) {
        System.out.println("1. Solicitar Matrícula em Turma");
        System.out.println("2. Cancelar Matrícula Ativa");
        System.out.println("3. Consultar Histórico Escolar");
        System.out.println("4. Consultar Disciplinas e Turmas Disponíveis");
        System.out.println("5. Consultar Percentual de Frequência"); 
        System.out.println("6. Fazer Logout (Encerrar Sessão)");
        System.out.println("=========================================");
        System.out.print("Escolha uma opção: ");
        String op = scanner.nextLine().trim();

        try {
            switch (op) {
                case "1":
                    System.out.print("Código da Disciplina para se matricular: ");
                    String matD = scanner.nextLine().trim();
                    System.out.print("Período Letivo Vigente (ex: 2026.2): ");
                    String perD = scanner.nextLine().trim();

                    // CORREÇÃO: Encaminha exclusivamente para a AlunoCLI processar (Evita gravação e incremento duplicados)
                    alunoCLI.processar("solicitarMatricula " + matD + " " + perD);
                    break;

                case "2":
                    System.out.print("Código da Disciplina a ser cancelada: ");
                    String discCanc = scanner.nextLine().trim();
                    System.out.print("Período Letivo da Matrícula: ");
                    String perCanc = scanner.nextLine().trim();

                    matriculaService.cancelarMatricula(matriculaLogada, discCanc, perCanc);
                    System.out.println("Sucesso: Solicitação de cancelamento processada e gravada em disco.");
                    break;

                case "3":
                    alunoCLI.processar("consultarHistorico");
                    break;

                case "4":
                    alunoCLI.processar("listarTurmas");
                    break;

                case "5": // PROCESSAMENTO ADICIONADO (US28) - NOMES DE VARIÁVEIS CORRIGIDOS
                    System.out.print("Código da Disciplina para análise: ");
                    String codF = scanner.nextLine().trim();
                    System.out.print("Período Letivo da Cadeira: ");
                    String perF = scanner.nextLine().trim();

                    // Encaminha os parâmetros exclusivos para a AlunoCLI
                    alunoCLI.processar("consultarFrequencia " + codF + " " + perF);
                    break;

                case "6":
                    authCLI.processar("logout");
                    break;

                default:
                    System.out.println("Opção inválida!");
            }
        } catch (Exception e) {
            System.err.println("ERRO DE NEGÓCIO: " + e.getMessage());
        }
    }

    // MENU EXCLUSIVO DO PROFESSOR ADICIONADO (US27 - RF27)
    private void exibirMenuProfessor(Scanner scanner, String matriculaLogada) {
        System.out.println("1. Registrar Presença/Falta (Diário de Classe)");
        System.out.println("2. Fazer Logout (Encerrar Sessão)");
        System.out.println("=========================================");
        System.out.print("Escolha uma opção: ");
        String op = scanner.nextLine().trim();

        try {
            switch (op) {
                case "1":
                    System.out.print("Código da Disciplina da Turma: ");
                    String codD = scanner.nextLine().trim();
                    System.out.print("Período Letivo (ex: 2026.1): ");
                    String per = scanner.nextLine().trim();
                    System.out.print("Data da Aula (ex: 27/06/2026): ");
                    String data = scanner.nextLine().trim();

                    professorCLI.processar("registrarChamada " + codD + " " + per + " " + data);
                    break;
                case "2":
                    authCLI.processar("logout");
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        } catch (Exception e) {
            System.err.println("ERRO NO MÓDULO DO PROFESSOR: " + e.getMessage());
        }
    }
}