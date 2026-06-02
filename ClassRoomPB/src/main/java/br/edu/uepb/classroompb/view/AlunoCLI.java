package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

public class AlunoCLI {
    private final TurmaService turmaService;
    private final AutenticacaoService authService = AutenticacaoService.getInstancia();

    // Construtor recebendo o serviço central do ecossistema g5
    public AlunoCLI(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    public void processar(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] partes = input.trim().split("\\s+");
        String comando = partes[0];

        try {
            // Validação básica de sessão
            Usuario logado = authService.getUsuarioLogado();
            if (logado == null || !"ALUNO".equalsIgnoreCase(logado.getPerfil())) {
                System.err.println("ACESSO NEGADO: Apenas alunos autenticados podem executar ações neste módulo.");
                return;
            }

            if (comando.equalsIgnoreCase("solicitarMatricula")) {
                if (partes.length < 2) {
                    System.err.println("Erro: Parâmetro insuficiente. Uso correto: solicitarMatricula [codigo_disciplina]");
                    return;
                }

                String codigoDisciplina = partes[1];
                String matriculaAluno = logado.getMatricula(); // Captura a matrícula direto da sessão global

                // Aciona o motor de verificação da consistência acadêmica (Task 2110)
                turmaService.validarPreRequisitos(matriculaAluno, codigoDisciplina);

                // Se passar da validação sem lançar exceção, continua o fluxo normal da matrícula
                System.out.println("SUCESSO: Pré-requisitos validados! Processando sua matrícula na disciplina " + codigoDisciplina + "...");
                
            } else if (comando.equalsIgnoreCase("cancelarMatricula")) {
                System.out.println("[Módulo Aluno] Comando 'cancelarMatricula' em desenvolvimento.");
                
            } else if (comando.equalsIgnoreCase("consultarHistorico")) {
                System.out.println("[Módulo Aluno] Exibindo Histórico Acadêmico do Aluno...");
                
            } else {
                System.out.println("Erro: Comando '" + comando + "' não reconhecido no Módulo do Aluno.");
            }

        } catch (ValidacaoException e) {
            // REQUISITO CENTRAL DA TASK 2111: Interceptador visual limpo para quebra de dependências
            System.out.println("\n---------------------------------------------------------");
            System.out.println("        ⚠️ OPERAÇÃO IMPEDIDA POR CONSISTÊNCIA ACADÊMICA ⚠️");
            System.out.println("---------------------------------------------------------");
            System.out.println(e.getMessage());
            System.out.println("Dica: Entre em contato com a coordenação do seu curso ou");
            System.out.println("regularize as matérias base pendentes para liberar a vaga.");
            System.out.println("---------------------------------------------------------\n");
            
        } catch (Exception e) {
            System.err.println("ERRO INTERNO: " + e.getMessage());
        }
    }
}