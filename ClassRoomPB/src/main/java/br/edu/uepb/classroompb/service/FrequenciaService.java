package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

import java.util.ArrayList;
import java.util.List;

public class FrequenciaService {
    private final TurmaRepository turmaRepository;
    private final MatriculaRepository matriculaRepository;
    private final FrequenciaRepository frequenciaRepository;

    public FrequenciaService(TurmaRepository turmaRepository, MatriculaRepository matriculaRepository, FrequenciaRepository frequenciaRepository) {
        this.turmaRepository = turmaRepository;
        this.matriculaRepository = matriculaRepository;
        this.frequenciaRepository = frequenciaRepository;
    }

     // Orquestra e valida o lançamento de chamada em lote de uma aula.
    public void registrarChamadaLote(String matriculaProfessor, String codigoDisciplina, String periodo, String dataAula, List<Matricula> alunosComStatus) 
            throws ValidacaoException {
        
        // 1. BARREIRA: Localiza e valida a existência física da turma
        List<Turma> turmas = turmaRepository.buscarTodas();
        Turma turmaAlvo = null;
        for (Turma t : turmas) {
            if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && t.getPeriodo().equalsIgnoreCase(periodo)) {
                turmaAlvo = t;
                break;
            }
        }

        if (turmaAlvo == null) {
            throw new ValidacaoException("Erro: Nenhuma turma ofertada para a disciplina '" + codigoDisciplina + "' no período '" + periodo + "'.");
        }

        // 2. BARREIRA: Segurança de Perfil - Valida se o professor logado é o real dono da turma (RF27)
        if (!turmaAlvo.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
            throw new ValidacaoException("Erro de Segurança: Você não possui permissão para lançar frequências na turma de outro docente.");
        }

        if (alunosComStatus == null || alunosComStatus.isEmpty()) {
            throw new ValidacaoException("Erro: Nenhum registro de frequência foi enviado para processamento.");
        }

        // 3. PROCESSAMENTO: Transforma a lista validada em objetos de frequência
        List<Frequencia> loteParaSalvar = new ArrayList<>();
        for (Matricula m : alunosComStatus) {
            Frequencia.TipoFrequencia statusChamada;
            if (m.getStatus() == Matricula.StatusMatricula.SOLICITADA) {
                statusChamada = Frequencia.TipoFrequencia.FALTA;
            } else {
                statusChamada = Frequencia.TipoFrequencia.PRESENCA;
            }

            loteParaSalvar.add(new Frequencia(dataAula, m.getMatriculaAluno(), codigoDisciplina, periodo, statusChamada));
        }

        frequenciaRepository.salvarLote(loteParaSalvar);
    }

     // US28/US29 Computa automaticamente a taxa percentual de assiduidade do estudante.
    public br.edu.uepb.classroompb.model.DesempenhoFrequencia calcularPercentualFrequencia(String matriculaAluno, String codigoDisciplina, String periodo) 
            throws ValidacaoException {
        
        // 1. BARREIRA: Valida se a turma física de fato existe no catálogo
        boolean turmaExiste = false;
        for (Turma t : turmaRepository.buscarTodas()) {
            if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina) && t.getPeriodo().equalsIgnoreCase(periodo)) {
                turmaExiste = true;
                break;
            }
        }
        if (!turmaExiste) {
            throw new ValidacaoException("Erro Analítico: A turma informada não existe no sistema corporativo.");
        }

        // 2. Coleta o histórico do aluno gravado no banco plano
        List<Frequencia> historico = frequenciaRepository.buscarPorAlunoEDisciplina(matriculaAluno, codigoDisciplina, periodo);
        
        int totalAulas = historico.size();
        int presencas = 0;
        int faltas = 0;

        for (Frequencia f : historico) {
            if (f.getStatus() == Frequencia.TipoFrequencia.PRESENCA) {
                presencas++;
            } else {
                faltas++;
            }
        }

        // 3. REGRA DE CRITÉRIO (US28/US29): Se nenhuma chamada foi realizada, o padrão regulamentar é 100.0%
        double percentual = 100.0;
        if (totalAulas > 0) {
            percentual = ((double) presencas / totalAulas) * 100.0;
        }

        return new br.edu.uepb.classroompb.model.DesempenhoFrequencia(totalAulas, presencas, faltas, percentual);
    }
}