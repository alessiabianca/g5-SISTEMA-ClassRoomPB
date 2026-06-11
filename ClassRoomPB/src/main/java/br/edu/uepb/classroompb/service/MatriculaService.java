package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioAlunoException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

import java.util.ArrayList;
import java.util.List;

public class MatriculaService {
    private final TurmaRepository turmaRepository;
    private final MatriculaRepository matriculaRepository; 

    public MatriculaService(TurmaRepository turmaRepository, MatriculaRepository matriculaRepository) {
        this.turmaRepository = turmaRepository;
        this.matriculaRepository = matriculaRepository;
    }

    public Matricula solicitarMatricula(String matriculaAluno, String codigoDisciplina, String periodo) 
            throws ChoqueHorarioAlunoException, ValidacaoException {
        
        Turma turma = buscarTurmaNoRepositorio(codigoDisciplina, periodo);
        if (turma == null) {
            throw new ValidacaoException("Turma não encontrada para esta disciplina no período informado.");
        }

        List<Matricula> matriculasAtuais = matriculaRepository.buscarTodas();

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

        Matricula novaMatricula = new Matricula(matriculaAluno, codigoDisciplina, periodo, statusFinal);
        matriculaRepository.salvar(novaMatricula);
        
        return novaMatricula;
    }

    /**
     * Mecanismo de Fila (Task 2)
     * Retorna a lista de alunos em espera para uma turma, mantendo estritamente a ordem de chegada (ordem do arquivo).
     */
    public List<Matricula> obterFilaDeEspera(String codigoDisciplina, String periodo) {
        List<Matricula> fila = new ArrayList<>();
        List<Matricula> todas = matriculaRepository.buscarTodas();
        
        for (Matricula m : todas) {
            if (m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) &&
                m.getPeriodo().equalsIgnoreCase(periodo) &&
                m.getStatus() == Matricula.StatusMatricula.ESPERA) {
                fila.add(m);
            }
        }
        return fila;
    }

    public void validarChoqueHorarioAluno(String matriculaAluno, String codigoNovaDisciplina, String periodo, List<Matricula> matriculasExistentes) 
            throws ChoqueHorarioAlunoException, ValidacaoException {
        
        Turma novaTurma = buscarTurmaNoRepositorio(codigoNovaDisciplina, periodo);
        if (novaTurma == null) {
            throw new ValidacaoException("Ação bloqueada: A turma para a disciplina '" + codigoNovaDisciplina + "' não está ofertada no período " + periodo + ".");
        }
        
        String horarioNovaTurma = novaTurma.getHorario();

        List<String> disciplinasDoAluno = new ArrayList<>();
        for (Matricula m : matriculasExistentes) {
            if (m.getMatriculaAluno().equalsIgnoreCase(matriculaAluno) && 
                m.getPeriodo().equalsIgnoreCase(periodo)) {
                
                if (m.getStatus() == Matricula.StatusMatricula.CONFIRMADA || 
                    m.getStatus() == Matricula.StatusMatricula.SOLICITADA) {
                    
                    disciplinasDoAluno.add(m.getCodigoDisciplina());
                }
            }
        }

        List<Turma> todasAsTurmas = turmaRepository.buscarTodas();
        for (Turma turmaExistente : todasAsTurmas) {
            if (turmaExistente.getPeriodo().equalsIgnoreCase(periodo)) {
                
                if (disciplinasDoAluno.contains(turmaExistente.getCodigoDisciplina())) {
                    
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