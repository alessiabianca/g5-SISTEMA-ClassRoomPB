package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;

public class CoordenadorCLI {
    private final TurmaService turmaService;

    public CoordenadorCLI() {
        this.turmaService = new TurmaService(new TurmaRepository());
    }

    public void processar(String input) {
        String[] partes = input.split(" ");
        String comando = partes[0];

        try {
            if (comando.equals("ofertarTurma")) {
                if (partes.length < 7) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso: ofertarTurma <disciplina> <professor> <periodo> <vagas> <horario> <sala>");
                    return;
                }
                String disciplina = partes[1];
                String professor = partes[2];
                String periodo = partes[3];
                int vagas = Integer.parseInt(partes[4]);
                String horario = partes[5];
                String sala = partes[6];

                turmaService.ofertarTurma(disciplina, professor, periodo, vagas, horario, sala);
                System.out.println("SUCESSO: Turma ofertada com sucesso!");

            } else if (comando.equals("editarTurma")) {
                if (partes.length < 6) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso: editarTurma <disciplina> <periodo> <novasVagas> <novoHorario> <novaSala>");
                    return;
                }
                String disciplina = partes[1];
                String periodo = partes[2];
                int novasVagas = Integer.parseInt(partes[3]);
                String novoHorario = partes[4];
                String novaSala = partes[5];

                turmaService.editarTurma(disciplina, periodo, novasVagas, novoHorario, novaSala);
                System.out.println("SUCESSO: Turma editada com sucesso!");

            } else if (comando.equals("cancelarTurma")) {
                if (partes.length < 3) {
                    System.err.println("Erro: Parâmetros insuficientes. Uso: cancelarTurma <disciplina> <periodo>");
                    return;
                }
                String disciplina = partes[1];
                String periodo = partes[2];

                turmaService.cancelarTurma(disciplina, periodo);
                System.out.println("SUCESSO: Turma cancelada com sucesso!");

            } else {
                System.out.println("[Módulo Coordenador] Comando '" + comando + "' ainda não implementado.");
            }
            
        } catch (NumberFormatException e) {
            System.err.println("ERRO: O campo vagas deve ser um número inteiro.");
        } catch (IllegalArgumentException e) {
            System.err.println("ERRO DE VALIDAÇÃO: " + e.getMessage());
        } catch (ChoqueHorarioException e) {
            System.err.println("ERRO DE NEGÓCIO: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERRO INTERNO: " + e.getMessage());
        }
    }
}