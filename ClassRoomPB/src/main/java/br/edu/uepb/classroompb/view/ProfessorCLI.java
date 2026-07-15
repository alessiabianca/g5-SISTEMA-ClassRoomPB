package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.FrequenciaService;
import br.edu.uepb.classroompb.service.NotaService; // Novo serviço importado
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProfessorCLI {
    private final TurmaService turmaService;
    private final FrequenciaService frequenciaService;
    private final NotaService notaService; // Injeção do novo serviço de notas
    private final AutenticacaoService authService = AutenticacaoService.getInstancia();

    // Construtor atualizado para receber o NotaService
    public ProfessorCLI(TurmaService turmaService, FrequenciaService frequenciaService, NotaService notaService) {
        this.turmaService = turmaService;
        this.frequenciaService = frequenciaService;
        this.notaService = notaService;
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

            // ====================================================================
            // [TASK 2472] - NOVO COMANDO: lancarNota
            // ====================================================================
            if (comando.equalsIgnoreCase("lancarNota")) {
                if (partes.length < 5) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso correto: lancarNota [matricula_aluno] [codigo_disciplina] [etapa] [nota]");
                    return;
                }

                String matriculaAluno = partes[1];
                String codigoDisciplina = partes[2];
                int etapa;
                double valorNota;

                try {
                    etapa = Integer.parseInt(partes[3]);
                    valorNota = Double.parseDouble(partes[4]);
                } catch (NumberFormatException e) {
                    System.err.println("Erro: Etapa e Nota devem ser valores numéricos válidos.");
                    return;
                }

                String matriculaProfessor = logado.getMatricula();

                // 1. Localizar o período ativo da turma para automatizar o preenchimento do histórico
                String periodoAtivo = null;
                for (Turma t : turmaService.listarTurmasDisponiveis()) {
                    if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && 
                        t.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
                        periodoAtivo = t.getPeriodo();
                        break;
                    }
                }

                if (periodoAtivo == null) {
                    throw new ValidacaoException("Ação Recusada: Não foi encontrada nenhuma turma sob sua responsabilidade para esta disciplina.");
                }

                // 2. Executa a regra de negócio e persiste o registro localmente
                notaService.lancarNota(matriculaProfessor, matriculaAluno, codigoDisciplina, periodoAtivo, etapa, valorNota);

                // 3. Renderiza o comprovante de lançamento estilizado no console
                System.out.println("\n=========================================================");
                System.out.println("            🧾 COMPROVANTE DE LANÇAMENTO DE NOTA         ");
                System.out.println("=========================================================");
                System.out.println(" STATUS DO REGISTRO : ✅ HOMOLOGADO E PUBLICADO");
                System.out.println(" ALUNO AVALIADO     : " + matriculaAluno);
                System.out.println(" DISCIPLINA / TURMA : " + codigoDisciplina.toUpperCase() + " (" + periodoAtivo + ")");
                System.out.println(" ETAPA AVALIATIVA   : " + etapa + "ª ETAPA");
                System.out.println(" VALOR ATRIBUÍDO    : ⭐ " + String.format("%.1f", valorNota));
                System.out.println("---------------------------------------------------------");
                System.out.println(" Sistema ClassRoomPB - Registro seguro de desempenho.");
                System.out.println("=========================================================\n");

            // ====================================================================
            // [US35] - NOVO COMANDO: editarNota (Retificação de Notas)
            // ====================================================================
            } else if (comando.equalsIgnoreCase("editarNota")) {
                if (partes.length < 5) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso correto: editarNota [matricula_aluno] [codigo_disciplina] [etapa] [novo_valor]");
                    return;
                }

                String matriculaAluno = partes[1];
                String codigoDisciplina = partes[2];
                int etapa;
                double novoValor;

                try {
                    etapa = Integer.parseInt(partes[3]);
                    novoValor = Double.parseDouble(partes[4]);
                } catch (NumberFormatException e) {
                    System.err.println("Erro: Etapa e Nota devem ser valores numéricos válidos.");
                    return;
                }

                String matriculaProfessor = logado.getMatricula();

                // 1. Localizar o período ativo da turma para automatizar o preenchimento
                String periodoAtivo = null;
                for (Turma t : turmaService.listarTurmasDisponiveis()) {
                    if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && 
                        t.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
                        periodoAtivo = t.getPeriodo();
                        break;
                    }
                }

                if (periodoAtivo == null) {
                    throw new ValidacaoException("Ação Recusada: Não foi encontrada nenhuma turma sob sua responsabilidade para esta disciplina.");
                }

                // 2. Executa a regra de negócio de retificação com bloqueio por encerramento
                notaService.retificarNota(matriculaProfessor, matriculaAluno, codigoDisciplina, periodoAtivo, etapa, novoValor);

                // 3. Renderiza o comprovante de retificação estilizado no console
                System.out.println("\n=========================================================");
                System.out.println("          🔄 COMPROVANTE DE RETIFICAÇÃO DE NOTA          ");
                System.out.println("=========================================================");
                System.out.println(" STATUS DO REGISTRO : ✅ RETIFICAÇÃO HOMOLOGADA");
                System.out.println(" ALUNO RETIFICADO   : " + matriculaAluno);
                System.out.println(" DISCIPLINA / TURMA : " + codigoDisciplina.toUpperCase() + " (" + periodoAtivo + ")");
                System.out.println(" ETAPA RETIFICADA   : " + etapa + "ª ETAPA");
                System.out.println(" NOVO VALOR         : ⭐ " + String.format("%.1f", novoValor));
                System.out.println("---------------------------------------------------------");
                System.out.println(" Sistema ClassRoomPB - Retificação segura de desempenho.");
                System.out.println("=========================================================\n");

            } else if (comando.equalsIgnoreCase("registrarChamada")) {
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
                System.out.println(" Registrado por: Prof. " + matriculaProfessor);
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

    private class RegistroChamadaDTO {
        String matriculaAluno;
        String status;

        RegistroChamadaDTO(String matriculaAluno, String status) {
            this.matriculaAluno = matriculaAluno;
            this.status = status;
        }
    }
}