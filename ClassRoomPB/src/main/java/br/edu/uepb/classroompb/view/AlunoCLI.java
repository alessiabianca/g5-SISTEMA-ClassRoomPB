package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioAlunoException; // IMPORTANTE: Importar a nova exceção
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.List;

public class AlunoCLI {
    private final TurmaService turmaService;
    private final AutenticacaoService authService = AutenticacaoService.getInstancia();

    // Construtor recebendo o serviço central do ecossistema g5
    public AlunoCLI(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    public void processar(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] partes = input.trim().split("\\s+");
        String comando = partes[0];

        try {
            // Validação básica de sessão
            Usuario logado = authService.getUsuarioLogado();
            if (logado == null || !"ALUNO".equalsIgnoreCase(logado.getPerfil())) {
                System.err.println("ACESSO NEGADO: Apenas alunos autenticados podem executar ações neste módulo.");
                return;
            }

            if (comando.equalsIgnoreCase("solicitarMatricula")) {
                // REQUISITO ATUALIZADO TASK 2104: Exige código da disciplina e período letivo
                if (partes.length < 3) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso correto: solicitarMatricula [codigo_disciplina] [codigo_periodo]");
                    return;
                }

                String codigoDisciplina = partes[1];
                String codigoPeriodo = partes[2];
                String matriculaAluno = logado.getMatricula(); // Captura a matrícula direto da sessão global

                // 1. Aciona o motor de verificação da consistência acadêmica (Lógica mantida da US18)
                turmaService.validarPreRequisitos(matriculaAluno, codigoDisciplina);

                // ADAPTAÇÃO DA US15 (RF19): Invoca o motor antichoques para a grade do aluno antes de efetivar
                turmaService.validarChoqueHorarioAluno(matriculaAluno, codigoDisciplina, codigoPeriodo);

                // 2. Aciona o motor de solicitação e efetivação de matrícula (Lógica central da Task 2103)
                turmaService.solicitarMatricula(matriculaAluno, codigoDisciplina, codigoPeriodo);
                
                System.out.println("SUCESSO: Matrícula processada e efetivada com sucesso na disciplina " + codigoDisciplina + " (" + codigoPeriodo + ")!");
                
            } else if (comando.equalsIgnoreCase("consultarTurmas")) {
                // Requisito mantido da Task 2101
                List<Turma> ofertas = turmaService.listarTurmasDisponiveis();

                if (ofertas.isEmpty()) {
                    System.out.println("\n---------------------------------------------------------");
                    System.out.println("⚠️ Não há disciplinas ou turmas ofertadas no momento. ⚠️");
                    System.out.println("---------------------------------------------------------\n");
                } else {
                    System.out.println("\n=========================================================");
                    System.out.println("        📚 TURMAS E DISCIPLINAS OFERTADAS DISPINÍVEIS     ");
                    System.out.println("=========================================================");
                    for (Turma turma : ofertas) {
                        System.out.println("📖 Disciplina: " + turma.getCodigoDisciplina());
                        System.out.println("👨‍🏫 Professor : " + turma.getMatriculaProfessor());
                        System.out.println("📅 Período   : " + turma.getPeriodo());
                        System.out.println("⏰ Horário   : " + turma.getHorario());
                        System.out.println("🏫 Sala      : " + turma.getSala());
                        System.out.println("👥 Vagas     : " + turma.getVagasOcupadas() + " / " + turma.getVagas());
                        System.out.println("---------------------------------------------------------");
                    }
                    System.out.println("Fim da listagem de turmas.\n");
                }

            } else if (comando.equalsIgnoreCase("cancelarMatricula")) {
                System.out.println("[Módulo Aluno] Comando 'cancelarMatricula' em development.");
                
            } else if (comando.equalsIgnoreCase("consultarHistorico")) {
                System.out.println("[Módulo Aluno] Exibindo Histórico Acadêmico do Aluno...");
                
            } else {
                System.out.println("Erro: Comando '" + comando + "' não reconhecido no Módulo do Aluno.");
            }

        } catch (ChoqueHorarioAlunoException e) {
            // ADAPTAÇÃO DA US15 / RF19: Captura visual específica do conflito de grade horária do estudante
            System.out.println("\n---------------------------------------------------------");
            System.out.println("            ⚠️ CONFLITO DE GRADE DETECTADO ⚠️");
            System.out.println("---------------------------------------------------------");
            System.out.println(e.getMessage());
            System.out.println("Ação cancelada para evitar choque de horários nas suas turmas.");
            System.out.println("---------------------------------------------------------\n");

        } catch (ValidacaoException e) {
            // REQUISITO DE CAPTURA DA TASK 2104 E TASK 2111
            System.out.println("\n---------------------------------------------------------");
            System.out.println("            ⚠️ OPERAÇÃO DE MATRÍCULA RECUSADA ⚠️");
            System.out.println("---------------------------------------------------------");
            System.out.println(e.getMessage());
            System.out.println("Dica: Verifique se há vagas, se cumpre as dependências base");
            System.out.println("ou se o período letivo informado encontra-se ativo.");
            System.out.println("---------------------------------------------------------\n");
            
        } catch (Exception e) {
            System.err.println("ERRO INTERNO NO MÓDULO DO ALUNO: " + e.getMessage());
        }
    }
}