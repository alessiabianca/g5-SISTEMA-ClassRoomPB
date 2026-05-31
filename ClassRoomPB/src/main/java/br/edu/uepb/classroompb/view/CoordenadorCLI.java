package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ChoqueSalaException; 
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

public class CoordenadorCLI {
    private final TurmaService turmaService;

    public CoordenadorCLI() {
        this.turmaService = new TurmaService(new TurmaRepository(), new PeriodoRepository(), new DisciplinaRepository());
    }

    public void processar(String input) {
        String[] partes = input.split(" ");
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
                
                // ADAPTAÇÃO US12: Passando o perfil do usuário logado (logado.getPerfil()) como o 7º parâmetro requisitado pelo Service
                turmaService.ofertarTurma(partes[1], partes[2], partes[3], Integer.parseInt(partes[4]), partes[5], partes[6], logado.getPerfil());
                System.out.println("SUCESSO: Turma ofertada com sucesso!");

            } else if (comando.equals("editarTurma")) {
                if (partes.length < 6) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso: editarTurma <disciplina> <periodo> <novasVagas> <novoHorario> <novaSala>");
                    return;
                }
                turmaService.editarTurma(partes[1], partes[2], Integer.parseInt(partes[3]), partes[4], partes[5]);
                System.out.println("SUCESSO: Turma editada com sucesso!");

            } else if (comando.equals("cancelarTurma")) {
                if (partes.length < 3) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso: cancelarTurma <disciplina> <periodo>");
                    return;
                }
                turmaService.cancelarTurma(partes[1], partes[2]);
                System.out.println("SUCESSO: Turma cancelada com sucesso!");

            } else {
                System.out.println("[Módulo Coordenador] Comando '" + comando + "' ainda não implementado.");
            }
            
        } catch (NumberFormatException e) {
            System.err.println("ERRO: O campo vagas deve ser um número inteiro.");
        } catch (ValidacaoException e) {
            System.err.println("ERRO DE VALIDACAO: " + e.getMessage());
        } catch (ChoqueHorarioException e) {
            // ADAPTAÇÃO US12 (Task 3): Captura específica do choque de horário do professor
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