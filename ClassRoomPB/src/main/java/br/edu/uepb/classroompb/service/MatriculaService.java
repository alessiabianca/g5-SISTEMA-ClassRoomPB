package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioAlunoException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

import java.util.ArrayList;
import java.util.List;

public class MatriculaService {
    private final TurmaRepository turmaRepository;
    private final MatriculaRepository matriculaRepository; 
    private final PeriodoRepository periodoRepository;

    public MatriculaService(TurmaRepository turmaRepository, MatriculaRepository matriculaRepository, PeriodoRepository periodoRepository) {
        this.turmaRepository = turmaRepository;
        this.matriculaRepository = matriculaRepository;
        this.periodoRepository = periodoRepository;
    }

    /**
     * Solisita a matrícula de um estudante.
     * [TASK 2280] Implementação e manutenção do Algoritmo FIFO (First-In, First-Out) para a Fila de Espera.
     * Garante que novos registros com status ESPERA entrem estritamente na cauda (tail) da estrutura.
     */
    public Matricula solicitarMatricula(String matriculaAluno, String codigoDisciplina, String periodo) 
            throws ChoqueHorarioAlunoException, ValidacaoException {
        
        Turma turma = buscarTurmaNoRepositorio(codigoDisciplina, periodo);
        if (turma == null) {
            throw new ValidacaoException("Turma não encontrada para esta disciplina no período informado.");
        }

        // Recupera a lista mantendo fielmente a ordem cronológica de inserção obtida do arquivo plano
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
            // Se o limite foi atingido, o aluno é enviado de forma ordenada para o fim (cauda) da lista de espera
            statusFinal = Matricula.StatusMatricula.ESPERA;
        }

        Matricula novaMatricula = new Matricula(matriculaAluno, codigoDisciplina, periodo, statusFinal);
        
        // A persistência via append garante que o novo elemento se posicione estritamente no final físico do arquivo (tail)
        matriculaRepository.salvar(novaMatricula);
        
        return novaMatricula;
    }

    public void cancelarMatricula(String matriculaAluno, String codigoDisciplina, String periodo) throws ValidacaoException {
        Periodo periodoLetivo = periodoRepository.buscarPorCodigo(periodo);
        
        if (periodoLetivo == null) {
            throw new ValidacaoException("Período letivo não encontrado.");
        }
        
        if (!periodoLetivo.isAbertoParaMatriculas()) {
            throw new ValidacaoException("Ação bloqueada: Cancelamento não permitido. O período letivo '" + periodo + "' não está aberto para modificações.");
        }

        List<Matricula> matriculasAtuais = matriculaRepository.buscarTodas();
        Matricula matriculaParaRemover = null;

        for (Matricula m : matriculasAtuais) {
            if (m.getMatriculaAluno().equalsIgnoreCase(matriculaAluno) &&
                m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) &&
                m.getPeriodo().equalsIgnoreCase(periodo)) {
                matriculaParaRemover = m;
                break;
            }
        }

        if (matriculaParaRemover == null) {
            throw new ValidacaoException("Matrícula não encontrada.");
        }

        matriculasAtuais.remove(matriculaParaRemover);
        matriculaRepository.atualizarArquivoCompleto(matriculasAtuais);
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