// src/main/java/br/edu/uepb/classroompb/service/TurmaService.java
package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;

import java.util.List;

public class TurmaService {
    private final TurmaRepository turmaRepository;
    private final PeriodoRepository periodoRepository;

    public TurmaService(TurmaRepository turmaRepository, PeriodoRepository periodoRepository) {
        this.turmaRepository = turmaRepository;
        this.periodoRepository = periodoRepository;
    }

    public void ofertarTurma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, String horario, String sala) {
        // Validação da Task 1836: Impedir oferta de turma sem professor responsável (RF13)
        if (matriculaProfessor == null || matriculaProfessor.trim().isEmpty()) {
            throw new IllegalArgumentException("Ação bloqueada: Nao eh possivel ofertar uma turma sem um professor responsavel.");
        }

        List<Turma> todasAsTurmas = turmaRepository.buscarTodas();

        for (Turma turmaExistente : todasAsTurmas) {
            if (turmaExistente.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
                if (turmaExistente.getPeriodo().equalsIgnoreCase(periodo) && turmaExistente.getHorario().equalsIgnoreCase(horario)) {
                    throw new ChoqueHorarioException("Choque de horário detetado para o professor nesta mesma combinação de período e horário.");
                }
            }
        }

        Turma novaTurma = new Turma(codigoDisciplina, matriculaProfessor, periodo, vagas, horario, sala);
        turmaRepository.salvar(novaTurma);
    }

    public void cancelarTurma(String codigoDisciplina, String periodo) {
        validarStatusPeriodo(periodo); 

        List<Turma> turmas = turmaRepository.buscarTodas();
        boolean turmaEncontrada = turmas.removeIf(t -> 
            t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && 
            t.getPeriodo().equalsIgnoreCase(periodo)
        );
        
        if (!turmaEncontrada) {
            throw new IllegalArgumentException("Turma não encontrada para a disciplina e período informados.");
        }
        
        turmaRepository.atualizarArquivoCompleto(turmas);
    }

    public void editarTurma(String codigoDisciplina, String periodo, int novasVagas, String novoHorario, String novaSala) {
        validarStatusPeriodo(periodo); 

        List<Turma> turmas = turmaRepository.buscarTodas();
        boolean turmaEncontrada = false;

        for (Turma t : turmas) {
            if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && t.getPeriodo().equalsIgnoreCase(periodo)) {
                t.setVagas(novasVagas);
                t.setHorario(novoHorario);
                t.setSala(novaSala);
                turmaEncontrada = true;
                break;
            }
        }

        if (!turmaEncontrada) {
            throw new IllegalArgumentException("Turma não encontrada para edição.");
        }

        turmaRepository.atualizarArquivoCompleto(turmas);
    }

    private void validarStatusPeriodo(String codigoPeriodo) {
        Periodo periodoLetivo = periodoRepository.buscarPorCodigo(codigoPeriodo);
        
        if (periodoLetivo != null) {
            String status = periodoLetivo.getStatus().toUpperCase();
            if (status.equals("INICIADO") || status.equals("ENCERRADO")) {
                throw new IllegalStateException("Ação bloqueada: O período letivo '" + codigoPeriodo + "' já está " + status + ".");
            }
        }
    }
}