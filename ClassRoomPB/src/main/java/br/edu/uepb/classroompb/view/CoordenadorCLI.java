package br.edu.uepb.classroompb.view;

public class CoordenadorCLI {
    public void processar(String input) {
        String comando = input.split(" ")[0];
        System.out.println("[CoordenadorCLI] Processando comando de coordenador: " + comando);
        // Implementar lógica de chamada aos Services de Coordenador (Turmas, Disciplinas)
    }
}
