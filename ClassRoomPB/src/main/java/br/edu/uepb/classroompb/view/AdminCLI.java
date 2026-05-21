// src/main/java/br/edu/uepb/classroompb/view/AdminCLI.java
package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.service.PeriodoService;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

public class AdminCLI {
    private final PeriodoService periodoService;

    // Construtor para receber o serviço de períodos
    public AdminCLI(PeriodoService periodoService) {
        this.periodoService = periodoService;
    }

    public void processar(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        // Divide o input por espaços para pegar o comando e os parâmetros
        String[] partes = input.split(" ");
        String comando = partes[0];

        switch (comando) {
            case "cadastrarPeriodo":
                if (partes.length < 2) {
                    System.err.println("Erro: Uso correto: cadastrarPeriodo <codigo>");
                    return;
                }
                try {
                    periodoService.cadastrarPeriodo(partes[1]);
                    System.out.println("Sucesso: Periodo " + partes[1] + " cadastrado como PLANEJADO.");
                } catch (ValidacaoException e) {
                    System.err.println("Erro de Validacao: " + e.getMessage());
                }
                break;

            case "ativarPeriodo":
                if (partes.length < 2) {
                    System.err.println("Erro: Uso correto: ativarPeriodo <codigo>");
                    return;
                }
                try {
                    periodoService.activarPeriodo(partes[1]);
                    System.out.println("Sucesso: Periodo " + partes[1] + " agora esta INICIADO.");
                } catch (ValidacaoException e) {
                    System.err.println("Erro de Validacao: " + e.getMessage());
                }
                break;

            default:
                // Mantém o comportamento padrão que os outros integrantes deixaram para os demais comandos
                System.out.println("[Módulo Administrador] Comando recebido: " + comando + " (Funcionalidade em desenvolvimento na US correspondente)");
                break;
        }
    }
}