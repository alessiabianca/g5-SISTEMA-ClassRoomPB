package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.FrequenciaService;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProfessorCLI {
    private final TurmaService turmaService;
    private final FrequenciaService frequenciaService;
    private final AutenticacaoService authService = AutenticacaoService.getInstancia();

    public ProfessorCLI(TurmaService turmaService, FrequenciaService frequenciaService) {
        this.turmaService = turmaService;
        this.frequenciaService = frequenciaService;
    }

    public void processar(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] partes = input.trim().split("\\s+");
        String comando = partes[0];

        try {
            Usuario logado = authService.getUsuarioLogado();
            if (logado == null || !"PROFESSOR".equalsIgnoreCase(logado.getPerfil())) {
                System.err.println("ACESSO NEGADO: Apenas professores autenticados podem executar ações neste módulo.");
                return;
            }

            // COMANDO CENTRAL DA US27 (RF27)

            if (comando.equalsIgnoreCase("registrarChamada")) {
                if (partes.length < 4) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso correto: registrarChamada [codigo_disciplina] [periodo] [data_aula]");
                    return;
                }

                String codigoDisciplina = partes[1];
                String codigoPeriodo = partes[2];
                String dataAula = partes[3];
                String matriculaProfessor = logado.getMatricula();

                br.edu.uepb.classroompb.repository.MatriculaRepository mRepo = new br.edu.uepb.classroompb.repository.MatriculaRepository();
                List<Matricula> todasMatriculas = mRepo.buscarTodas();
                List<String> matriculasAlunosDaTurma = new ArrayList<>();

                for (Matricula m : todasMatriculas) {
                    if (m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && 
                        m.getPeriodo().equalsIgnoreCase(codigoPeriodo) && 
                        m.getStatus() == Matricula.StatusMatricula.CONFIRMADA) {
                        matriculasAlunosDaTurma.add(m.getMatriculaAluno());
                    }
                }

                if (matriculasAlunosDaTurma.isEmpty()) {
                    System.out.println("\n Não há alunos com matrícula CONFIRMADA nesta turma para registrar chamada.\n");
                    return;
                }

                Scanner scanner = new Scanner(System.in);
                System.out.println("\n=========================================================");
                System.out.println("          📋 INICIANDO DIÁRIO DE CLASSE ITERATIVO        ");
                System.out.println("=========================================================");
                System.out.println(" DISCIPLINA : " + codigoDisciplina + " | PERÍODO: " + codigoPeriodo);
                System.out.println(" DATA AULA  : " + dataAula);
                System.out.println("---------------------------------------------------------");
                System.out.println(" Digite 'P' para PRESENÇA ou 'F' para FALTA para cada aluno:");
                System.out.println("---------------------------------------------------------");

                List<RegistroChamadaDTO> loteDTO = new ArrayList<>();

                for (String matriculaAluno : matriculasAlunosDaTurma) {
                    while (true) {
                        System.out.print(" Aluno: " + matriculaAluno + " [P/F]: ");
                        String entrada = scanner.nextLine().trim().toUpperCase();
                        
                        if (entrada.equals("P")) {
                            loteDTO.add(new RegistroChamadaDTO(matriculaAluno, "PRESENCA"));
                            break;
                        } else if (entrada.equals("F")) {
                            loteDTO.add(new RegistroChamadaDTO(matriculaAluno, "FALTA"));
                            break;
                        } else {
                            System.out.println("   ❌ Opção inválida! Digite apenas 'P' ou 'F'.");
                        }
                    }
                }

                List<br.edu.uepb.classroompb.model.Frequencia> loteParaSalvar = new ArrayList<>();
                for (RegistroChamadaDTO dto : loteDTO) {
                    br.edu.uepb.classroompb.model.Frequencia.TipoFrequencia tipo = 
                        dto.status.equals("FALTA") ? br.edu.uepb.classroompb.model.Frequencia.TipoFrequencia.FALTA 
                                                   : br.edu.uepb.classroompb.model.Frequencia.TipoFrequencia.PRESENCA;
                    
                    loteParaSalvar.add(new br.edu.uepb.classroompb.model.Frequencia(
                        dataAula, dto.matriculaAluno, codigoDisciplina, codigoPeriodo, tipo
                    ));
                }

                br.edu.uepb.classroompb.repository.FrequenciaRepository freqRepo = new br.edu.uepb.classroompb.repository.FrequenciaRepository();
                
                List<Turma> turmas = turmaService.listarTurmasDisponiveis();
                Turma turmaAlvo = null;
                for (Turma t : turmas) {
                    if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && t.getPeriodo().equalsIgnoreCase(codigoPeriodo)) {
                        turmaAlvo = t;
                        break;
                    }
                }
                
                if (turmaAlvo != null && !turmaAlvo.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
                    throw new ValidacaoException("Erro de Segurança: Você não possui permissão para lançar frequências na turma de outro docente.");
                }

                freqRepo.salvarLote(loteParaSalvar);

                System.out.println("\n=========================================================");
                System.out.println("           🧾 DIÁRIO DE CLASSE FECHADO COM SUCESSO       ");
                System.out.println("=========================================================");
                System.out.println(" STATUS DA CHAMADA  : ✅ HOMOLOGADA E SALVA EM DISCO");
                System.out.println(" DISCIPLINA / TURMA : " + codigoDisciplina + " (" + codigoPeriodo + ")");
                System.out.println(" DATA DE REGISTRO   : " + dataAula);
                System.out.println(" TOTAL DE ALUNOS    : " + loteParaSalvar.size() + " avaliados.");
                System.out.println("---------------------------------------------------------");
                System.out.println(" Sistema ClassRoomPB - Padrão de Estilo Visual Corporativo.");
                System.out.println("=========================================================\n");

            } else {
                System.out.println("Erro: Comando '" + comando + "' não reconhecido no Módulo do Professor.");
            }

        } catch (ValidacaoException e) {
            System.out.println("\n---------------------------------------------------------");
            System.out.println("          ⚠️ OPERAÇÃO RECUSADA PELO SISTEMA ⚠️");
            System.out.println("---------------------------------------------------------");
            System.out.println(e.getMessage());
            System.out.println("Dica: Certifique-se de que você é o professor responsável");
            System.out.println("vinculado a esta turma específica.");
            System.out.println("---------------------------------------------------------\n");
            
        } catch (Exception e) {
            System.err.println("ERRO INTERNO NO MÓDULO DO PROFESSOR: " + e.getMessage());
        }
    }

    // CORREÇÃO: Classe interna colocada corretamente no escopo da classe principal e sem o modificador 'static' impeditivo
    private class RegistroChamadaDTO {
        String matriculaAluno;
        String status;

        RegistroChamadaDTO(String matriculaAluno, String status) {
            this.matriculaAluno = matriculaAluno;
            this.status = status;
        }
    }
}