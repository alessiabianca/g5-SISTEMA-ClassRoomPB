package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.DesempenhoFrequencia;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.MatriculaService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.FrequenciaService;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioAlunoException; 
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.List;

public class AlunoCLI {
    private final TurmaService turmaService;
    private final MatriculaService matriculaService;
    private final AutenticacaoService authService = AutenticacaoService.getInstancia();

    // Construtor atualizado para receber os serviços compartilhados do ecossistema TerminalCLI
    public AlunoCLI(TurmaService turmaService, MatriculaService matriculaService) {
        this.turmaService = turmaService;
        this.matriculaService = matriculaService;
    }

    public void processar(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] partes = input.trim().split("\\s+");
        String comando = partes[0];

        try {
            Usuario logado = authService.getUsuarioLogado();
            if (logado == null || !"ALUNO".equalsIgnoreCase(logado.getPerfil())) {
                System.err.println("ACESSO NEGADO: Apenas alunos autenticados podem executar ações neste módulo.");
                return;
            }

            if (comando.equalsIgnoreCase("solicitarMatricula")) {
                if (partes.length < 3) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso correto: solicitarMatricula [codigo_disciplina] [codigo_periodo]");
                    return;
                }

                String codigoDisciplina = partes[1];
                String codigoPeriodo = partes[2];
                String matriculaAluno = logado.getMatricula();

                Matricula matriculaProcessada = matriculaService.solicitarMatricula(matriculaAluno, codigoDisciplina, codigoPeriodo);
                
                Turma turmaMatriculada = null;
                for (Turma t : turmaService.listarTurmasDisponiveis()) {
                    if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && t.getPeriodo().equalsIgnoreCase(codigoPeriodo)) {
                        turmaMatriculada = t;
                        break;
                    }
                }

                if (matriculaProcessada.getStatus() == Matricula.StatusMatricula.ESPERA) {
                    System.out.println("\n=========================================================");
                    System.out.println("                 ⚠️ LISTA DE ESPERA ⚠️                   ");
                    System.out.println("=========================================================");
                    System.out.println(" AVISO: A turma atingiu o limite máximo de vagas.");
                    System.out.println(" Você foi adicionado à fila de espera em ordem de chegada.");
                    System.out.println(" MATRÍCULA DO ALUNO    : " + matriculaAluno);
                    System.out.println(" CÓDIGO DA DISCIPLINA  : " + codigoDisciplina);
                    System.out.println("=========================================================\n");
                } else {
                    System.out.println("\n=========================================================");
                    System.out.println("           🧾 COMPROVANTE DE MATRÍCULA EMITIDO           ");
                    System.out.println("=========================================================");
                    System.out.println(" STATUS DA SOLICITAÇÃO : ✅ " + matriculaProcessada.getStatus() + " (AUTOMÁTICA)");
                    System.out.println(" MATRÍCULA DO ALUNO    : " + matriculaAluno);
                    System.out.println(" CÓDIGO DA DISCIPLINA  : " + codigoDisciplina);
                    System.out.println(" PERÍODO LETIVO        : " + codigoPeriodo);
                    if (turmaMatriculada != null) {
                        System.out.println(" HORÁRIO DA TURMA      : " + turmaMatriculada.getHorario());
                        System.out.println(" SALA ALOCADA          : " + turmaMatriculada.getSala());
                    }
                    System.out.println("---------------------------------------------------------");
                    System.out.println(" Sistema ClassRoomPB - Vínculo acadêmico seguro e validado.");
                    System.out.println("=========================================================\n");
                }

            } else if (comando.equalsIgnoreCase("listarTurmas")) {
                List<Turma> turmas = turmaService.listarTurmasDisponiveis();
                if (turmas.isEmpty()) {
                    System.out.println("Nenhuma turma disponível para matrícula no momento.");
                } else {
                    System.out.println("\n--- Turmas Ofertadas no Sistema ---");
                    for (Turma t : turmas) {
                        System.out.println("Disciplina: " + t.getCodigoDisciplina() + 
                                           " | Período: " + t.getPeriodo() + 
                                           " | Horário: " + t.getHorario() + 
                                           " | Sala: " + t.getSala() + 
                                           " | Vagas Livres: " + (t.getVagas() - t.getVagasOcupadas()));
                    }
                    System.out.println("Fim da listagem de turmas.\n");
                }

            } else if (comando.equalsIgnoreCase("cancelarMatricula")) {
                if (partes.length < 3) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso correto: cancelarMatricula [codigo_disciplina] [codigo_periodo]");
                    return;
                }

                String codigoDisciplina = partes[1];
                String codigoPeriodo = partes[2];
                String matriculaAluno = logado.getMatricula();

                matriculaService.cancelarMatricula(matriculaAluno, codigoDisciplina, codigoPeriodo);
                
                System.out.println("\n=========================================================");
                System.out.println("           🗑️ CANCELAMENTO DE MATRÍCULA EFETUADO         ");
                System.out.println("=========================================================");
                System.out.println(" MATRÍCULA DO ALUNO    : " + matriculaAluno);
                System.out.println(" CÓDIGO DA DISCIPLINA  : " + codigoDisciplina);
                System.out.println(" PERÍODO LETIVO        : " + codigoPeriodo);
                System.out.println("---------------------------------------------------------");
                System.out.println(" Sua matrícula foi removida com sucesso e a vaga liberada.");
                System.out.println("=========================================================\n");
                
            // ====================================================================
            // COMANDO CENTRAL DA US28 (RF28) - PAINEL ANALÍTICO ESTILIZADO
            // ====================================================================
            } else if (comando.equalsIgnoreCase("consultarFrequencia")) {
                if (partes.length < 3) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso correto: consultarFrequencia [codigo_disciplina] [codigo_periodo]");
                    return;
                }

                String codigoDisciplina = partes[1];
                String codigoPeriodo = partes[2];
                String matriculaAluno = logado.getMatricula();

                // Instanciação isolada do serviço analítico de frequências seguindo as regras de estilo
                TurmaRepository tRepo = new TurmaRepository();
                MatriculaRepository mRepo = new MatriculaRepository();
                FrequenciaRepository fRepo = new FrequenciaRepository();
                FrequenciaService freqService = new FrequenciaService(tRepo, mRepo, fRepo);

                // Executa a inteligência analítica de agregação e computação de taxas
                DesempenhoFrequencia desempenho = freqService.calcularPercentualFrequencia(matriculaAluno, codigoDisciplina, codigoPeriodo);

                // EXIBIÇÃO VISUAL PADRONIZADA 
                System.out.println("\n=========================================================");
                System.out.println("          📊 EXTRATO DE ASSIDUIDADE AUTOMÁTICO           ");
                System.out.println("=========================================================");
                System.out.println(" MATRÍCULA DO ALUNO    : " + matriculaAluno);
                System.out.println(" DISCIPLINA AVALIADA   : " + codigoDisciplina + " | PERÍODO: " + codigoPeriodo);
                System.out.println("---------------------------------------------------------");
                System.out.println(" TOTAL DE AULAS MINISTRADAS : " + desempenho.getTotalAulas());
                System.out.println(" NÚMERO DE PRESENÇAS        : " + desempenho.getPresencas());
                System.out.println(" NÚMERO DE FALTAS ACUMULADAS: " + desempenho.getFaltas());
                System.out.println("---------------------------------------------------------");
                
                String percentualFormatado = String.format("%.1f", desempenho.getPercentualFrequencia()) + "%";
                System.out.println(" PERCENTUAL CONSOLIDADO     : " + percentualFormatado);
                
                // Alerta Visual de Segurança de Notas/Faltas (Crivo regulatório de 75%)
                if (desempenho.getPercentualFrequencia() < 75.0) {
                    System.out.println(" STATUS DA ASSIDUIDADE      : ⚠️ ALERTA: RISCO DE REPROVAÇÃO POR FALTA!");
                } else {
                    System.out.println(" STATUS DA ASSIDUIDADE      : ✅ SITUAÇÃO REGULAR (DENTRO DA MÉTRICA)");
                }
                System.out.println("---------------------------------------------------------");
                System.out.println(" Sistema ClassRoomPB - Análise estatística de aproveitamento.");
                System.out.println("=========================================================\n");

            } else if (comando.equalsIgnoreCase("consultarHistorico")) {
                System.out.println("[Módulo Aluno] Exibindo Histórico Acadêmico do Aluno...");
                
            } else {
                System.out.println("Erro: Comando '" + comando + "' não reconhecido no Módulo do Aluno.");
            }
            
        } catch (ChoqueHorarioAlunoException e) {
            System.out.println("\n---------------------------------------------------------");
            System.out.println("            ⚠️ CONFLITO DE GRADE DETECTADO ⚠️");
            System.out.println("---------------------------------------------------------");
            System.out.println(e.getMessage());
            System.out.println("Ação cancelada para evitar choque de horários na sua grade.");
            System.out.println("---------------------------------------------------------\n");

        } catch (ValidacaoException e) {
            System.out.println("\n---------------------------------------------------------");
            System.out.println("            ⚠️ OPERAÇÃO RECUSADA ⚠️");
            System.out.println("---------------------------------------------------------");
            System.out.println(e.getMessage());
            System.out.println("---------------------------------------------------------\n");
            
        } catch (Exception e) {
            System.err.println("ERRO INTERNO NO MÓDULO DO ALUNO: " + e.getMessage());
        }
    }
}