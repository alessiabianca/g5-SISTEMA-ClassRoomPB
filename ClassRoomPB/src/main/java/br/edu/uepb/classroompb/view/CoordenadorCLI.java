package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ChoqueSalaException; 
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.List;

public class CoordenadorCLI {
    private final TurmaService turmaService;

    public CoordenadorCLI() {
        this.turmaService = new TurmaService(new TurmaRepository(), new PeriodoRepository(), new DisciplinaRepository());
    }

    public void processar(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] partes = input.trim().split("\\s+");
        String comando = partes[0];

        try {
            Usuario logado = AutenticacaoService.getInstancia().getUsuarioLogado();
            if (logado == null || !"COORDENADOR".equalsIgnoreCase(logado.getPerfil())) {
                System.err.println("ACESSO NEGADO: Apenas usuarios autenticados com o perfil de Coordenador podem executar esta acao.");
                return;
            }

            if (comando.equals("ofertarTurma")) {
                if (partes.length < 7) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso: ofertarTurma <disciplina> <professor> <periodo> <vagas> <horario> <sala>");
                    return;
                }
                
                turmaService.ofertarTurma(partes[1], partes[2], partes[3], Integer.parseInt(partes[4]), partes[5], partes[6], logado.getPerfil());
                System.out.println("SUCESSO: Turma ofertada com sucesso!");

            } else if (comando.equals("editarTurma")) {
                if (partes.length < 7) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso: editarTurma <disciplina> <periodo> <novoProfessor> <novasVagas> <novoHorario> <novaSala>");
                    return;
                }
                
                String codigoDisciplina = partes[1];
                String periodo = partes[2];
                String novoProfessor = partes[3];
                int novasVagas = Integer.parseInt(partes[4]);
                String novoHorario = partes[5];
                String novaSala = partes[6];

                turmaService.editarTurma(codigoDisciplina, periodo, novoProfessor, novasVagas, novoHorario, novaSala);
                System.out.println("SUCESSO: Turma editada com sucesso!");

            } else if (comando.equals("cancelarTurma")) {
                if (partes.length < 3) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso: cancelarTurma <disciplina> <periodo>");
                    return;
                }
                turmaService.cancelarTurma(partes[1], partes[2]);
                System.out.println("SUCESSO: Turma cancelada com sucesso!");

            } else if (comando.equals("exibirListaEspera")) {
                // [TASK 2283] Mapeamento do comando de visualização da lista de espera
                if (partes.length < 3) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso: exibirListaEspera <codigoDisciplina> <codigoPeriodo>");
                    return;
                }
                
                String codigoDisciplina = partes[1];
                String codigoPeriodo = partes[2];
                
                List<Matricula> fila = turmaService.obterListaEspera(codigoDisciplina, codigoPeriodo);
                
                System.out.println("\n=========================================================");
                System.out.println("      📋 FILA DE ESPERA OFICIAL — COORDENAÇÃO            ");
                System.out.println("=========================================================");
                System.out.println(" TURMA: " + codigoDisciplina.toUpperCase() + " | PERÍODO: " + codigoPeriodo);
                System.out.println("---------------------------------------------------------");
                
                if (fila.isEmpty()) {
                    System.out.println(" STATUS: Não há alunos aguardando na fila desta turma.");
                } else {
                    int posicao = 1;
                    for (Matricula m : fila) {
                        System.out.println(" " + posicao + "º Lugar - Matrícula: " + m.getMatriculaAluno());
                        posicao++;
                    }
                }
                System.out.println("=========================================================\n");

            } else {
                System.out.println("[Módulo Coordenador] Comando '" + comando + "' ainda não implementado.");
            }
            
        } catch (NumberFormatException e) {
            System.err.println("ERRO: O campo vagas deve ser un número inteiro.");
        } catch (ValidacaoException e) {
            System.err.println("ERRO DE VALIDAÇÃO: " + e.getMessage());
        } catch (ChoqueHorarioException e) {
            System.err.println("[CONFLITO DE HORÁRIO] " + e.getMessage());
        } catch (ChoqueSalaException e) {
            System.err.println("ERRO DE ALOCACAO: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("ERRO DE VALIDAÇÃO: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("ERRO DE ESTADO: " + e.getMessage()); 
        } catch (Exception e) {
            System.err.println("ERRO INTERNO: " + e.getMessage());
        }
    }
}