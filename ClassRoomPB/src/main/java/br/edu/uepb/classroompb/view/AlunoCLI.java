package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.DesempenhoFrequencia;
import br.edu.uepb.classroompb.model.ExtratoDiarioAluno;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.DiarioService;
import br.edu.uepb.classroompb.service.FrequenciaService;
import br.edu.uepb.classroompb.service.HistoricoService;
import br.edu.uepb.classroompb.service.MatriculaService;
import br.edu.uepb.classroompb.service.NotaService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioAlunoException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.List;

public class AlunoCLI {
  private final TurmaService turmaService;
  private final MatriculaService matriculaService;
  private final HistoricoService historicoService;
  private final DiarioService diarioService;
  private final AutenticacaoService authService = AutenticacaoService.getInstancia();
  private final NotaRepository notaRepository = new NotaRepository();

  public AlunoCLI(
      TurmaService turmaService,
      MatriculaService matriculaService,
      HistoricoService historicoService) {
    this(
        turmaService,
        matriculaService,
        historicoService,
        new DiarioService(
            new br.edu.uepb.classroompb.repository.DiarioRepository(),
            new TurmaRepository(),
            new br.edu.uepb.classroompb.repository.UsuarioRepository()));
  }

  public AlunoCLI(
      TurmaService turmaService,
      MatriculaService matriculaService,
      HistoricoService historicoService,
      DiarioService diarioService) {
    this.turmaService = turmaService;
    this.matriculaService = matriculaService;
    this.historicoService = historicoService;
    this.diarioService = diarioService;
  }

  public void processar(String input) {
    if (input == null || input.trim().isEmpty()) {
      return;
    }

    String[] partes = input.trim().split("\\s+");
    String comando = partes[0];

    try {
      Usuario logado = authService.getUsuarioLogado();
      if (logado == null || !"ALUNO".equalsIgnoreCase(logado.getPerfil())) {
        System.err.println(
            "ACESSO NEGADO: Apenas alunos autenticados podem executar ações neste módulo.");
        return;
      }

      if (comando.equalsIgnoreCase("solicitarMatricula")) {
        if (partes.length < 3) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso correto: solicitarMatricula [codigo_disciplina] [codigo_periodo]");
          return;
        }

        String codigoDisciplina = partes[1];
        String codigoPeriodo = partes[2];
        String matriculaAluno = logado.getMatricula();

        Matricula matriculaProcessada =
            matriculaService.solicitarMatricula(matriculaAluno, codigoDisciplina, codigoPeriodo);

        if (matriculaProcessada.getStatus() == Matricula.StatusMatricula.ESPERA) {
          List<Matricula> filaEspera =
              turmaService.obterListaEspera(codigoDisciplina, codigoPeriodo);
          int posicaoNaLista = filaEspera.size();

          System.out.println("\n=========================================================");
          System.out.println("                 ⚠️ LISTA DE ESPERA ⚠️                   ");
          System.out.println("=========================================================");
          System.out.println(
              " Turma lotada! Você foi adicionado à lista de espera na posição "
                  + posicaoNaLista
                  + ".");
          System.out.println(" MATRÍCULA DO ALUNO    : " + matriculaAluno);
          System.out.println(" CÓDIGO DA DISCIPLINA  : " + codigoDisciplina.toUpperCase());
          System.out.println("=========================================================\n");
        } else {
          System.out.println("\n=========================================================");
          System.out.println("            🧾 COMPROVANTE DE MATRÍCULA EMITIDO           ");
          System.out.println("=========================================================");
          System.out.println(
              " STATUS DA SOLICITAÇÃO : ✅ " + matriculaProcessada.getStatus() + " (AUTOMÁTICA)");
          System.out.println(" MATRÍCULA DO ALUNO    : " + matriculaAluno);
          System.out.println(" CÓDIGO DA DISCIPLINA  : " + codigoDisciplina);
          System.out.println(" PERÍODO LETIVO        : " + codigoPeriodo);
          System.out.println("---------------------------------------------------------");
          System.out.println(" Sistema ClassRoomPB - Vínculo acadêmico seguro e validado.");
          System.out.println("=========================================================\n");
        }

      } else if (comando.equalsIgnoreCase("listarTurmas")) {
        List<Turma> turmas = turmaService.listarTurmasDisponiveis();
        if (turmas.isEmpty()) {
          System.out.println("Nenhuma turma disponível para matrícula no momento.");
        } else {
          System.out.println("\n--- Turmas Ofertadas no Sistema ---");
          for (Turma t : turmas) {
            System.out.println(
                "Disciplina: "
                    + t.getCodigoDisciplina()
                    + " | Período: "
                    + t.getPeriodo()
                    + " | Vagas Livres: "
                    + (t.getVagas() - t.getVagasOcupadas()));
          }
          System.out.println("Fim da listagem de turmas.\n");
        }

      } else if (comando.equalsIgnoreCase("cancelarMatricula")) {
        if (partes.length < 3) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso correto: cancelarMatricula [codigo_disciplina] [codigo_periodo]");
          return;
        }

        String codigoDisciplina = partes[1];
        String codigoPeriodo = partes[2];
        String matriculaAluno = logado.getMatricula();

        matriculaService.cancelarMatricula(matriculaAluno, codigoDisciplina, codigoPeriodo);

        System.out.println("\n=========================================================");
        System.out.println("           🗑️ CANCELAMENTO DE MATRÍCULA EFETUADO         ");
        System.out.println("=========================================================");
        System.out.println(" MATRÍCULA DO ALUNO    : " + matriculaAluno);
        System.out.println(" CÓDIGO DA DISCIPLINA  : " + codigoDisciplina);
        System.out.println(" PERÍODO LETIVO        : " + codigoPeriodo);
        System.out.println("---------------------------------------------------------");
        System.out.println(" Sua matrícula foi removida com sucesso e a vaga liberada.");
        System.out.println("=========================================================\n");

      } else if (comando.equalsIgnoreCase("consultarFrequencia")) {
        if (partes.length < 3) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso correto: consultarFrequencia [codigo_disciplina] [codigo_periodo]");
          return;
        }

        String codigoDisciplina = partes[1];
        String codigoPeriodo = partes[2];
        String matriculaAluno = logado.getMatricula();

        TurmaRepository tRepo = new TurmaRepository();
        MatriculaRepository mRepo = new MatriculaRepository();
        FrequenciaRepository fRepo = new FrequenciaRepository();
        FrequenciaService freqService =
            new FrequenciaService(
                tRepo,
                mRepo,
                fRepo,
                notaRepository,
                new br.edu.uepb.classroompb.repository.DiarioRepository(),
                new br.edu.uepb.classroompb.repository.AulaRepository());

        DesempenhoFrequencia desempenho =
            freqService.calcularPercentualFrequencia(
                matriculaAluno, codigoDisciplina, codigoPeriodo);

        double nota1 = desempenho.getNotaEtapa1();
        double nota2 = desempenho.getNotaEtapa2();
        double mediaFinal = (nota1 + nota2) / 2.0;

        System.out.println("\n=========================================================");
        System.out.println("          📊 EXTRATO DE DESEMPENHO E ASSIDUIDADE         ");
        System.out.println("=========================================================");
        System.out.println(" MATRÍCULA DO ALUNO    : " + matriculaAluno);
        System.out.println(
            " DISCIPLINA AVALIADA   : "
                + codigoDisciplina.toUpperCase()
                + " | PERÍODO: "
                + codigoPeriodo);
        System.out.println("---------------------------------------------------------");
        System.out.println(" NOTA 1ª ETAPA              : " + String.format("%.1f", nota1));
        System.out.println(" NOTA 2ª ETAPA              : " + String.format("%.1f", nota2));
        System.out.println(" MÉDIA PARCIAL COMPUTADA    : ⭐ " + String.format("%.1f", mediaFinal));
        System.out.println("---------------------------------------------------------");
        System.out.println(" TOTAL DE AULAS MINISTRADAS : " + desempenho.getTotalAulas());
        System.out.println(" NÚMERO DE PRESENÇAS        : " + desempenho.getPresencas());
        System.out.println(" NÚMERO DE FALTAS ACUMULADAS: " + desempenho.getFaltas());
        System.out.println("---------------------------------------------------------");

        String percentualFormatado =
            String.format("%.1f", desempenho.getPercentualFrequencia()) + "%";
        System.out.println(" PERCENTUAL CONSOLIDADO     : " + percentualFormatado);

        if (desempenho.getPercentualFrequencia() < 75.0) {
          System.out.println(
              " STATUS DA ASSIDUIDADE      : ⚠️ ALERTA CRÍTICO: RISCO DE REPROVAÇÃO POR FALTA!");
        } else {
          System.out.println(
              " STATUS DA ASSIDUIDADE      : ✅ SITUAÇÃO REGULAR (DENTRO DA MÉTRICA)");
        }
        System.out.println("---------------------------------------------------------");
        System.out.println(" Sistema ClassRoomPB - Boletim unificado de notas e faltas.");
        System.out.println("=========================================================\n");

      } else if (comando.equalsIgnoreCase("consultarNotas")) {
        if (partes.length < 2) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso correto: consultarNotas [codigo_periodo]");
          return;
        }

        String codigoPeriodo = partes[1];
        String matriculaAluno = logado.getMatricula();

        TurmaRepository tRepo = new TurmaRepository();
        MatriculaRepository mRepo = new MatriculaRepository();
        br.edu.uepb.classroompb.repository.PeriodoRepository pRepo =
            new br.edu.uepb.classroompb.repository.PeriodoRepository();
        NotaService notaService =
            new NotaService(
                notaRepository,
                tRepo,
                mRepo,
                pRepo,
                new br.edu.uepb.classroompb.repository.AvaliacaoRepository(),
                new br.edu.uepb.classroompb.repository.DiarioRepository());

        List<Nota> boletim = notaService.buscarNotasPorAlunoEPeriodo(matriculaAluno, codigoPeriodo);

        System.out.println("\n=================================================================");
        System.out.println("              🎓 BOLETIM ACADÊMICO CONSOLIDADO                   ");
        System.out.println("=================================================================");
        System.out.println(
            " MATRÍCULA DO ALUNO : " + matriculaAluno + " | PERÍODO: " + codigoPeriodo);
        System.out.println("-----------------------------------------------------------------");
        System.out.printf(
            " %-15s | %-12s | %-12s | %-12s \n",
            "DISCIPLINA", "1ª ETAPA", "2ª ETAPA", "MÉDIA PARCIAL");
        System.out.println("-----------------------------------------------------------------");

        if (boletim.isEmpty()) {
          System.out.println("  ⚠️ Nenhuma matrícula confirmada localizada para este período.");
        } else {
          for (Nota n : boletim) {
            double media = (n.getNota1() + n.getNota2()) / 2.0;
            System.out.printf(
                " %-15s | %-12.1f | %-12.1f | ⭐ %-10.1f \n",
                n.getCodigoDisciplina().toUpperCase(), n.getNota1(), n.getNota2(), media);
          }
        }

        System.out.println("-----------------------------------------------------------------");
        System.out.println(" Sistema ClassRoomPB - Consulta rápida de desempenho acadêmico.");
        System.out.println("=================================================================\n");

      } else if (comando.equalsIgnoreCase("consultarSituacao")) {
        if (partes.length < 3) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso correto: consultarSituacao [codigo_disciplina] [codigo_periodo]");
          return;
        }

        String codigoDisciplina = partes[1];
        String codigoPeriodo = partes[2];
        String matriculaAluno = logado.getMatricula();

        TurmaRepository tRepo = new TurmaRepository();
        MatriculaRepository mRepo = new MatriculaRepository();
        FrequenciaRepository fRepo = new FrequenciaRepository();
        br.edu.uepb.classroompb.repository.NotaRepository nRepo =
            new br.edu.uepb.classroompb.repository.NotaRepository();
        FrequenciaService freqService =
            new FrequenciaService(
                tRepo,
                mRepo,
                fRepo,
                notaRepository,
                new br.edu.uepb.classroompb.repository.DiarioRepository(),
                new br.edu.uepb.classroompb.repository.AulaRepository());
        br.edu.uepb.classroompb.repository.AvaliacaoRepository avaliacaoRepo =
            new br.edu.uepb.classroompb.repository.AvaliacaoRepository();
        br.edu.uepb.classroompb.repository.DiarioRepository diarioRepo =
            new br.edu.uepb.classroompb.repository.DiarioRepository();

        br.edu.uepb.classroompb.service.SituacaoAcademicaService situacaoService =
            new br.edu.uepb.classroompb.service.SituacaoAcademicaService(
                nRepo, freqService, avaliacaoRepo, diarioRepo);

        br.edu.uepb.classroompb.service.SituacaoAcademicaService.ResultadoApuracao resultado =
            situacaoService.apurarSituacao(matriculaAluno, codigoDisciplina, codigoPeriodo);

        br.edu.uepb.classroompb.model.Nota notaAluno = resultado.getNota();
        double media = resultado.getMedia();
        br.edu.uepb.classroompb.model.StatusAcademico statusFinal = resultado.getStatus();
        DesempenhoFrequencia desempenhoFreq = resultado.getDesempenho();

        System.out.println("\n=========================================================");
        System.out.println("          📋 SITUAÇÃO ACADÊMICA CONSOLIDADA              ");
        System.out.println("=========================================================");
        System.out.println(" MATRÍCULA DO ALUNO    : " + matriculaAluno);
        System.out.println(
            " DISCIPLINA AVALIADA   : "
                + codigoDisciplina.toUpperCase()
                + " | PERÍODO: "
                + codigoPeriodo);
        System.out.println("---------------------------------------------------------");
        System.out.println(
            " NOTA 1 (AV1)               : " + String.format("%.1f", notaAluno.getNota1()));
        System.out.println(
            " NOTA 2 (AV2)               : " + String.format("%.1f", notaAluno.getNota2()));
        if (notaAluno.getNota3() >= 0) {
          System.out.println(
              " NOTA 3 (AV3)               : " + String.format("%.1f", notaAluno.getNota3()));
        }
        System.out.println(" MÉDIA ARITMÉTICA           : " + String.format("%.1f", media));
        System.out.println("---------------------------------------------------------");
        String freqFormatada =
            String.format("%.1f", desempenhoFreq.getPercentualFrequencia()) + "%";
        System.out.println(" FREQUÊNCIA CONSOLIDADA     : " + freqFormatada);
        System.out.println("---------------------------------------------------------");

        String iconeStatus;
        switch (statusFinal) {
          case APROVADO:
            iconeStatus = "✅ APROVADO";
            break;
          case RECUPERACAO:
            iconeStatus = "🔄 EM RECUPERAÇÃO";
            break;
          case REPROVADO_NOTA:
            iconeStatus = "❌ REPROVADO POR NOTA";
            break;
          case REPROVADO_FALTA:
            iconeStatus = "❌ REPROVADO POR FALTA";
            break;
          default:
            iconeStatus = statusFinal.name();
        }

        System.out.println(" SITUAÇÃO ACADÊMICA         : " + iconeStatus);
        System.out.println("---------------------------------------------------------");
        System.out.println(" Sistema ClassRoomPB - Apuração automatizada de resultados.");
        System.out.println("=========================================================\n");

      } else if (comando.equalsIgnoreCase("meusDiarios")
          || comando.equalsIgnoreCase("consultarDiarios")) {
        List<br.edu.uepb.classroompb.model.Diario> diarios =
            diarioService.consultarDiariosDoAluno(logado);
        System.out.println(
            "\n==========================================================================");
        System.out.println("                    MEUS DIARIOS DE MATRICULA");
        System.out.println(
            "==========================================================================");
        System.out.printf(
            " %-12s | %-12s | %-10s | %-14s | %-8s | %-8s%n",
            "DIARIO", "DISCIPLINA", "PERIODO", "PROFESSOR", "SALA", "STATUS");
        System.out.println(
            "--------------------------------------------------------------------------");
        if (diarios.isEmpty()) {
          System.out.println(" Nenhum diario encontrado para suas matriculas confirmadas.");
        } else {
          System.out.print(diarioService.formatarListaDiarios(diarios));
        }
        System.out.println(
            "==========================================================================\n");

      } else if (comando.equalsIgnoreCase("consultarDiario")) {
        if (partes.length != 2) {
          System.err.println("Erro: Uso: consultarDiario <codigo_diario>");
          return;
        }
        ExtratoDiarioAluno extrato = diarioService.consultarExtratoDoAluno(logado, partes[1]);
        System.out.println(
            "\n==========================================================================");
        System.out.println("                 EXTRATO DETALHADO DO DIARIO");
        System.out.println(
            "==========================================================================");
        System.out.print(diarioService.formatarExtratoAluno(extrato));
        System.out.println(
            "==========================================================================\n");

      } else if (comando.equalsIgnoreCase("consultarHistorico")) {
        List<br.edu.uepb.classroompb.model.Historico> historico =
            historicoService.consultarHistorico(logado.getMatricula());

        System.out.println(
            "\n=========================================================================================");
        System.out.println(
            "                         📜 HISTÓRICO ACADÊMICO CONSOLIDADO                              ");
        System.out.println(
            "=========================================================================================");
        System.out.println(
            " MATRÍCULA DO ALUNO : " + logado.getMatricula() + " | NOME: " + logado.getNome());
        System.out.println(
            "-----------------------------------------------------------------------------------------");
        System.out.printf(
            " %-12s | %-12s | %-16s | %-7s | %-7s | %-25s \n",
            "PERÍODO", "DISCIPLINA", "PROFESSOR", "MÉDIA", "FREQ", "SITUAÇÃO");
        System.out.println(
            "-----------------------------------------------------------------------------------------");

        if (historico.isEmpty()) {
          System.out.println(
              "  ⚠️ Nenhum registro histórico localizado (nenhum período encerrado com matrículas ativas).");
        } else {
          System.out.print(historicoService.formatarHistorico(historico));
        }
        System.out.println(
            "=========================================================================================\n");
      } else {
        System.out.println("Erro: Comando '" + comando + "' não reconhecido no Módulo do Aluno.");
      }

    } catch (ChoqueHorarioAlunoException e) {
      System.out.println("\n---------------------------------------------------------");
      System.out.println("            ⚠️ CONFLITO DE GRADE DETECTADO ⚠️");
      System.out.println("---------------------------------------------------------");
      System.out.println(e.getMessage());
      System.out.println("Ação cancelada para evitar choque de horários na sua grade.");
      System.out.println("---------------------------------------------------------\n");

    } catch (ValidacaoException e) {
      System.out.println("\n---------------------------------------------------------");
      System.out.println("            ⚠️ OPERAÇÃO RECUSADA ⚠️");
      System.out.println("---------------------------------------------------------");
      System.out.println(e.getMessage());
      System.out.println("---------------------------------------------------------\n");

    } catch (Exception e) {
      System.err.println("ERRO INTERNO NO MÓDULO DO ALUNO: " + e.getMessage());
    }
  }
}
