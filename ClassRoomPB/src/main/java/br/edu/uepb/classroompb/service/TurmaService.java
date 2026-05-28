package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.model.Disciplina;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ChoqueSalaException; 
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

import java.io.IOException;
import java.util.List;

public class TurmaService {
    private final TurmaRepository turmaRepository;
    private final PeriodoRepository periodoRepository;
    private final DisciplinaRepository disciplinaRepository;

    public TurmaService(TurmaRepository turmaRepository, PeriodoRepository periodoRepository, DisciplinaRepository disciplinaRepository) {
        this.turmaRepository = turmaRepository;
        this.periodoRepository = periodoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    // Adicionado ChoqueSalaException na assinatura do método para cumprir a Task 1907
    public void ofertarTurma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, String horario, String sala) 
            throws ValidacaoException, ChoqueHorarioException, ChoqueSalaException {
        
        // 1. Validação do limite de vagas (inteiro positivo exigido na US11)
        if (vagas <= 0) {
            throw new ValidacaoException("Acao bloqueada: O limite de vagas deve ser um inteiro positivo.");
        }

        // 2. Validação da US10: Validar se a disciplina informada realmente existe no sistema
        try {
            Disciplina disciplinaExistente = disciplinaRepository.buscarPorCodigo(codigoDisciplina);
            if (disciplinaExistente == null) {
                throw new ValidacaoException("Acao bloqueada: Nao eh possivel ofertar uma turma para uma disciplina inexistente.");
            }
        } catch (IOException e) {
            throw new ValidacaoException("Erro ao acessar o armazenamento de disciplinas: " + e.getMessage());
        }

        // 3. Validação da US10: Validar se o período letivo está com o status Ativo ("INICIADO")
        Periodo periodoLetivo = periodoRepository.buscarPorCodigo(periodo);
        if (periodoLetivo == null) {
            throw new ValidacaoException("Acao bloqueada: O periodo letivo informado nao existe.");
        }
        if (!periodoLetivo.isAbertoParaMatriculas()) {
            throw new ValidacaoException("Acao bloqueada: O periodo letivo '" + periodo + "' nao esta ativo (Status atual: " + periodoLetivo.getStatus() + ").");
        }

        // 4. Validação da Task 1836/US11: Impedir oferta de turma sem professor responsável (ou atributos vazios)
        if (matriculaProfessor == null || matriculaProfessor.trim().isEmpty()) {
            throw new IllegalArgumentException("Ação bloqueada: Nao eh possivel ofertar uma turma sem um professor responsavel.");
        }
        if (horario == null || horario.trim().isEmpty() || sala == null || sala.trim().isEmpty()) {
            throw new IllegalArgumentException("Ação bloqueada: Horario e sala sao atributos obrigatorios.");
        }

        // 5. Motores Antichoques (Task 1907)
        List<Turma> todasAsTurmas = turmaRepository.buscarTodas();
        for (Turma turmaExistente : todasAsTurmas) {
            // Regra só se aplica se for dentro do mesmo período letivo
            if (turmaExistente.getPeriodo().equalsIgnoreCase(periodo) && turmaExistente.getHorario().equalsIgnoreCase(horario)) {
                
                // Validação A: Choque de Horário do Professor
                if (turmaExistente.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
                    throw new ChoqueHorarioException("Choque de horário detetado para o professor nesta mesma combinação de período e horário.");
                }

                // Validação B: Choque de Sala (US11 / Task 1907)
                if (turmaExistente.getSala().equalsIgnoreCase(sala)) {
                    throw new ChoqueSalaException("Choque de sala detetado: A sala '" + sala + "' ja esta ocupada por outra turma neste mesmo horario.");
                }
            }
        }

        // Se passar em tudo, persiste a turma
        Turma novaTurma = new Turma(codigoDisciplina, matriculaProfessor, periodo, vagas, horario, sala);
        turmaRepository.salvar(novaTurma);
    }

    // =========================================================
    // MÉTODOS DA US14 - EDIÇÃO E CANCELAMENTO DE TURMAS
    // =========================================================

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