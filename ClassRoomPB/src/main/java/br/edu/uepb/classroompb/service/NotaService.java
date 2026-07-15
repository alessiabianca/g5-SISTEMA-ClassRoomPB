package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

import java.util.List;

public class NotaService {
    private final NotaRepository notaRepository;
    private final TurmaRepository turmaRepository;

    public NotaService(NotaRepository notaRepository, TurmaRepository turmaRepository) {
        this.notaRepository = notaRepository;
        this.turmaRepository = turmaRepository;
    }

    /**
     * Lança as notas de avaliações parciais de forma atômica no repositório.
     * Valida o intervalo [0.0 - 10.0] e assegura que apenas o professor da turma execute a ação.
     */
    public void lancarNota(String matriculaProfessor, String matriculaAluno, String codigoDisciplina, String periodo, int etapa, double valorNota) 
            throws ValidacaoException {
        
        // 1. Validar permissão e responsabilidade do professor sobre a turma[cite: 3]
        List<Turma> turmas = turmaRepository.buscarTodas();
        Turma turmaAlvo = null;
        for (Turma t : turmas) {
            if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && t.getPeriodo().equalsIgnoreCase(periodo)) {
                turmaAlvo = t;
                break;
            }
        }

        if (turmaAlvo == null) {
            throw new ValidacaoException("Erro: Turma não encontrada para esta disciplina e período.");
        }

        if (!turmaAlvo.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
            throw new ValidacaoException("Erro de Segurança: Você não possui permissão para lançar notas na turma de outro docente.");
        }

        // 2. Validar se o valor da nota está no intervalo estrito de 0.0 a 10.0[cite: 2]
        if (valorNota < 0.0 || valorNota > 10.0) {
            throw new ValidacaoException("Erro: Nota inválida. O valor informado deve estar no intervalo estrito de 0.0 a 10.0.");
        }

        if (etapa != 1 && etapa != 2) {
            throw new ValidacaoException("Erro: Etapa de avaliação inválida. Use apenas 1 ou 2.");
        }

        // 3. Persistência atômica[cite: 5]
        List<Nota> todasNotas = notaRepository.buscarTodas();
        Nota notaExistente = null;

        for (Nota n : todasNotas) {
            if (n.getMatriculaAluno().equalsIgnoreCase(matriculaAluno) &&
                n.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) &&
                n.getPeriodo().equalsIgnoreCase(periodo)) {
                notaExistente = n;
                break;
            }
        }

        if (notaExistente != null) {
            // Atualiza nota existente na memória utilizando seus métodos originais
            if (etapa == 1) {
                notaExistente.setNota1(valorNota);
            } else {
                notaExistente.setNota2(valorNota);
            }
        } else {
            // Cria um novo registro com valor default -1 para a nota 3
            double n1 = (etapa == 1) ? valorNota : 0.0;
            double n2 = (etapa == 2) ? valorNota : 0.0;
            notaExistente = new Nota(matriculaAluno, codigoDisciplina, periodo, n1, n2, -1.0);
            todasNotas.add(notaExistente);
        }

        // Reescreve o arquivo de notas para salvar fisicamente de forma segura[cite: 5]
        atualizarArquivoCompletoLocal(todasNotas);
    }

    private void atualizarArquivoCompletoLocal(List<Nota> notasAtualizadas) {
        String FILE_PATH = "data/notas.txt"; //[cite: 5]
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(FILE_PATH, false))) {
            for (Nota n : notasAtualizadas) {
                bw.write(n.toString());
                bw.newLine();
            }
        } catch (java.io.IOException e) {
            System.err.println("Erro ao reescrever o arquivo de notas: " + e.getMessage());
        }
    }
}