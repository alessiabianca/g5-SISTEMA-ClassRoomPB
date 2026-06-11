package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioAlunoException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

import java.util.ArrayList;
import java.util.List;

public class MatriculaService {
    private final TurmaRepository turmaRepository;
    // MatriculaRepository será injetado aqui na próxima task
    // private final MatriculaRepository matriculaRepository; 

    public MatriculaService(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }

    public Matricula solicitarMatricula(String matriculaAluno, String codigoDisciplina, String periodo, List<Matricula> matriculasAtuais) 
            throws ChoqueHorarioAlunoException, ValidacaoException {
        
        Turma turma = buscarTurmaNoRepositorio(codigoDisciplina, periodo);
        if (turma == null) {
            throw new ValidacaoException("Turma não encontrada para esta disciplina no período informado.");
        }

        validarChoqueHorarioAluno(matriculaAluno, codigoDisciplina, periodo, matriculasAtuais);

        long ocupacao = 0;
        for (Matricula m : matriculasAtuais) {
            if (m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && m.getPeriodo().equalsIgnoreCase(periodo)) {
                if (m.getStatus() == Matricula.StatusMatricula.CONFIRMADA || m.getStatus() == Matricula.StatusMatricula.SOLICITADA) {
                    ocupacao++;
                }
            }
        }

        Matricula.StatusMatricula statusFinal = Matricula.StatusMatricula.SOLICITADA;
        if (ocupacao >= turma.getVagas()) {
            statusFinal = Matricula.StatusMatricula.ESPERA;
        }

        return new Matricula(matriculaAluno, codigoDisciplina, periodo, statusFinal);
    }

    /**
     * Valida se o aluno possui choque de horário com as turmas onde ele já está matriculado.
     * Desenvolvido para cumprir o RF19.
     */
    public void validarChoqueHorarioAluno(String matriculaAluno, String codigoNovaDisciplina, String periodo, List<Matricula> matriculasExistentes) 
            throws ChoqueHorarioAlunoException, ValidacaoException {
        
        // 1. Localiza a turma física que o aluno está tentando se matricular para extrair o horário
        Turma novaTurma = buscarTurmaNoRepositorio(codigoNovaDisciplina, periodo);
        if (novaTurma == null) {
            throw new ValidacaoException("Ação bloqueada: A turma para a disciplina '" + codigoNovaDisciplina + "' não está ofertada no período " + periodo + ".");
        }
        
        String horarioNovaTurma = novaTurma.getHorario();

        // 2. Filtra todas as disciplinas que este aluno específico já está matriculado NESTE período letivo
        List<String> disciplinasDoAluno = new ArrayList<>();
        for (Matricula m : matriculasExistentes) {
            if (m.getMatriculaAluno().equalsIgnoreCase(matriculaAluno) && 
                m.getPeriodo().equalsIgnoreCase(periodo)) {
                
                // CORREÇÃO AQUI: Comparação direta usando o Enum tipado de forma segura
                if (m.getStatus() == Matricula.StatusMatricula.CONFIRMADA || 
                    m.getStatus() == Matricula.StatusMatricula.SOLICITADA) {
                    
                    disciplinasDoAluno.add(m.getCodigoDisciplina());
                }
            }
        }

        // 3. Varre as turmas das disciplinas encontradas para comparar as strings de horário (Motor Antichoques)
        List<Turma> todasAsTurmas = turmaRepository.buscarTodas();
        for (Turma turmaExistente : todasAsTurmas) {
            if (turmaExistente.getPeriodo().equalsIgnoreCase(periodo)) {
                
                // Se a turma pertence a uma das disciplinas que o aluno já está matriculado
                if (disciplinasDoAluno.contains(turmaExistente.getCodigoDisciplina())) {
                    
                    // Colisão de Strings na propriedade de horário (RF19)
                    if (turmaExistente.getHorario().equalsIgnoreCase(horarioNovaTurma)) {
                        throw new ChoqueHorarioAlunoException("Conflito de Grade: O aluno '" + matriculaAluno 
                            + "' já está matriculado na disciplina '" + turmaExistente.getCodigoDisciplina() 
                            + "' que ocorre no mesmo horário (" + horarioNovaTurma + ").");
                    }
                }
            }
        }
    }

    private Turma buscarTurmaNoRepositorio(String codigoDisciplina, String periodo) {
        List<Turma> turmas = turmaRepository.buscarTodas();
        for (Turma t : turmas) {
            if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && t.getPeriodo().equalsIgnoreCase(periodo)) {
                return t;
            }
        }
        return null;
    }
}