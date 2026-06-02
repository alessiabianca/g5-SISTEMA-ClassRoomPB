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
import java.util.ArrayList;

public class TurmaService {
    private final TurmaRepository turmaRepository;
    private final PeriodoRepository periodoRepository;
    private final DisciplinaRepository disciplinaRepository;

    public TurmaService(TurmaRepository turmaRepository, PeriodoRepository periodoRepository, DisciplinaRepository disciplinaRepository) {
        this.turmaRepository = turmaRepository;
        this.periodoRepository = periodoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    // ====================================================================
    // REQUISITO CENTRAL DA TASK 2110 (US18) - MOTOR DE VARREDURA HISTÓRICA
    // ====================================================================
    public void validarPreRequisitos(String matriculaAluno, String codigoDisciplina) throws ValidacaoException {
        try {
            // 1. Localiza a disciplina desejada no repositório oficial
            Disciplina disciplinaDesejada = disciplinaRepository.buscarPorCodigo(codigoDisciplina);
            if (disciplinaDesejada == null) {
                throw new ValidacaoException("Erro: Disciplina informada não existe no sistema.");
            }

            // 2. Obtém a lista de dependências/códigos obrigatórios da matéria
            List<String> preRequisitos = disciplinaDesejada.getPreRequisitosCodigos();
            
            // Se não houver pré-requisitos cadastrados, a operação é liberada direto
            if (preRequisitos == null || preRequisitos.isEmpty() || preRequisitos.contains("NENHUM")) {
                return;
            }

            // 3. Simula a varredura na base de dados de aprovações do estudante
            List<String> historicoAprovacoes = obterHistoricoAprovacoesAluno(matriculaAluno);

            // 4. Cruza os requisitos exigidos com o histórico acadêmico do solicitante
            List<String> pendencias = new ArrayList<>();
            for (String req : preRequisitos) {
                if (!historicoAprovacoes.contains(req)) {
                    pendencias.add(req);
                }
            }

            // Se houver qualquer quebra de pré-requisito, aborta com a lista detalhada
            if (!pendencias.isEmpty()) {
                throw new ValidacaoException("Erro de Consistência Acadêmica: O aluno não cumpre os pré-requisitos: " + String.join(", ", pendencias));
            }

        } catch (IOException e) {
            throw new ValidacaoException("Erro ao acessar a persistência para validar pré-requisitos: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar de simulação histórica (Será estendido e mockado nos testes da Task 2112)
     */
    private List<String> obterHistoricoAprovacoesAluno(String matriculaAluno) {
        List<String> aprovadas = new ArrayList<>();
        // Exemplo fixo inicial: Se for um estudante veterano específico para testes, simula histórico
        if ("202601".equals(matriculaAluno) || "VETERANO_01".equals(matriculaAluno)) {
            aprovadas.add("P1"); // Já pagou Programação I
            aprovadas.add("MAT_DISC");
        }
        return aprovadas;
    }

    // REQUISITO CENTRAL DA TASK 2108: Lógica de saldo de ocupação automatizada
    public void verificarDisponibilidadeVagas(Turma turma) throws ValidacaoException {
        if (turma == null) {
            throw new ValidacaoException("Erro: Turma inválida ou inexistente.");
        }
        if (turma.getVagasOcupadas() >= turma.getVagas()) { 
            throw new ValidacaoException("Erro: Não há vagas disponíveis nesta turma.");
        }
    }

    public void ofertarTurma(String codigoDisciplina, String matriculaProfessor, String periodo, int vagas, String horario, String sala, String papelUsuarioLogado) 
            throws ValidacaoException, ChoqueHorarioException, ChoqueSalaException {
        
        if (papelUsuarioLogado == null || !papelUsuarioLogado.equalsIgnoreCase("COORDENADOR")) {
            throw new ValidacaoException("Acesso negado: Apenas coordenadores podem cadastrar ou ofertar turmas.");
        }

        if (vagas <= 0) {
            throw new ValidacaoException("Acao bloqueada: O limite de vagas deve ser um inteiro positivo.");
        }

        try {
            Disciplina disciplinaExistente = disciplinaRepository.buscarPorCodigo(codigoDisciplina);
            if (disciplinaExistente == null) {
                throw new ValidacaoException("Acao bloqueada: Nao eh possivel ofertar uma turma para uma disciplina inexistente.");
            }
        } catch (IOException e) {
            throw new ValidacaoException("Erro ao acessar o armazenamento de disciplinas: " + e.getMessage());
        }

        Periodo periodoLetivo = periodoRepository.buscarPorCodigo(periodo);
        if (periodoLetivo == null) {
            throw new ValidacaoException("Acao bloqueada: O periodo letivo informado nao existe.");
        }
        if (!periodoLetivo.isAbertoParaMatriculas()) {
            throw new ValidacaoException("Acao bloqueada: O periodo letivo '" + periodo + "' nao esta ativo.");
        }

        if (matriculaProfessor == null || matriculaProfessor.trim().isEmpty()) {
            throw new IllegalArgumentException("Ação bloqueada: Nao eh possivel ofertar uma turma sem um professor responsavel.");
        }
        if (horario == null || horario.trim().isEmpty() || sala == null || sala.trim().isEmpty()) {
            throw new IllegalArgumentException("Ação bloqueada: Horario e sala sao atributos obrigatorios.");
        }

        List<Turma> todasAsTurmas = turmaRepository.buscarTodas();
        for (Turma turmaExistente : todasAsTurmas) {
            if (turmaExistente.getPeriodo().equalsIgnoreCase(periodo) && turmaExistente.getHorario().equalsIgnoreCase(horario)) {
                if (turmaExistente.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
                    throw new ChoqueHorarioException("Erro de Conflito: O professor '" + matriculaProfessor + "' já está alocado.");
                }
                if (turmaExistente.getSala().equalsIgnoreCase(sala)) {
                    throw new ChoqueSalaException("Choque de sala detetado: A sala '" + sala + "' ja esta ocupada.");
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
            t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && t.getPeriodo().equalsIgnoreCase(periodo)
        );
        if (!turmaEncontrada) {
            throw new IllegalArgumentException("Turma não encontrada para a disciplina e período informados.");
        }
        turmaRepository.atualizarArquivoCompleto(turmas);
    }

    public void editarTurma(String codigoDisciplina, String periodo, String novaMatriculaProfessor, int novasVagas, String novoHorario, String novaSala) {
        validarStatusPeriodo(periodo); 
        if (novaMatriculaProfessor == null || novaMatriculaProfessor.trim().isEmpty()) {
            throw new IllegalArgumentException("Ação bloqueada: Não é possível editar uma turma deixando-a sem um professor responsável.");
        }

        List<Turma> turmas = turmaRepository.buscarTodas();
        boolean turmaEncontrada = false;

        for (int i = 0; i < turmas.size(); i++) {
            Turma t = turmas.get(i);
            if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && t.getPeriodo().equalsIgnoreCase(periodo)) {
                Turma turmaAtualizada = new Turma(codigoDisciplina, novaMatriculaProfessor.trim(), periodo, novasVagas, t.getVagasOcupadas(), novoHorario, novaSala);
                turmas.set(i, turmaAtualizada);
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