package br.edu.uepb.classroompb.service;

import java.util.List;

import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;

public class TurmaService {
    private final TurmaRepository turmaRepository;

    public TurmaService(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }

    public void ofertarTurma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, String horario, String sala) {
        List<Turma> todasAsTurmas = turmaRepository.buscarTodas();

        for (Turma turmaExistente : todasAsTurmas) {
            if (turmaExistente.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
                boolean mesmoPeriodo = turmaExistente.getPeriodo().equalsIgnoreCase(periodo);
                boolean mesmoHorario = turmaExistente.getHorario().equalsIgnoreCase(horario);

                if (mesmoPeriodo && mesmoHorario) {
                    throw new ChoqueHorarioException("Choque de horário detectado para o professor nesta mesma combinação de período e horário.");
                }
            }
        }

        Turma novaTurma = new Turma(codigoDisciplina, matriculaProfessor, periodo, vagas, horario, sala);
        turmaRepository.salvar(novaTurma);
    }
}