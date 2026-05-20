package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;

import java.util.List;

public class TurmaService {
    private final TurmaRepository turmaRepository;

    public TurmaService(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }

    //Ofertar com validação de choque de horário
    public void ofertarTurma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, String horario, String sala) {
        List<Turma> todasAsTurmas = turmaRepository.buscarTodas();

        for (Turma turmaExistente : todasAsTurmas) {
            if (turmaExistente.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
                boolean mesmoPeriodo = turmaExistente.getPeriodo().equalsIgnoreCase(periodo);
                boolean mesmoHorario = turmaExistente.getHorario().equalsIgnoreCase(horario);

                if (mesmoPeriodo && mesmoHorario) {
                    throw new ChoqueHorarioException("Choque de horário detetado para o professor nesta mesma combinação de período e horário.");
                }
            }
        }

        Turma novaTurma = new Turma(codigoDisciplina, matriculaProfessor, periodo, vagas, horario, sala);
        turmaRepository.salvar(novaTurma);
    }

    //Cancelar Turma
    public void cancelarTurma(String codigoDisciplina, String periodo) {
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

    //Editar Turma
    public void editarTurma(String codigoDisciplina, String periodo, int novasVagas, String novoHorario, String novaSala) {
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
}