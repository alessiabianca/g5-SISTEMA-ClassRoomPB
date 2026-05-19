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

        if (comando.equals("ofertarTurma")) {
            if (partes.length < 7) {
                System.err.println("Erro: Parâmetros insuficientes. Uso: ofertarTurma codigoDisciplina professor periodo vagas horario sala");
                return;
            }

            try {
                String codigoDisciplina = partes[1];
                String professor = partes[2];
                String periodo = partes[3];
                int vagas = Integer.parseInt(partes[4]);
                String horario = partes[5];
                String sala = partes[6];

                turmaService.ofertarTurma(codigoDisciplina, professor, periodo, vagas, horario, sala);
                System.out.println("SUCESSO: Turma ofertada com sucesso!");

            } catch (NumberFormatException e) {
                System.err.println("Erro: O campo vagas deve ser um número inteiro.");
            } catch (ChoqueHorarioException e) {
                System.err.println("ERRO DE NEGÓCIO: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Erro inesperado: " + e.getMessage());
            }
        } else {
            System.out.println("[Módulo Coordenador] Comando '" + comando + "' ainda não implementado.");
        }
    }
}