package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.OcupacaoVagasTurma;
import br.edu.uepb.classroompb.model.RelatorioOcupacaoVagas;
import br.edu.uepb.classroompb.model.RelatorioReprovacaoDisciplina;
import br.edu.uepb.classroompb.model.ReprovacaoDisciplina;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.DiarioService;
import br.edu.uepb.classroompb.service.HistoricoService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ChoqueSalaException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.List;

public class CoordenadorCLI {
  private final TurmaService turmaService;
  private final HistoricoService historicoService;
  private final DiarioService diarioService;
  private final UsuarioRepository usuarioRepository;

  public CoordenadorCLI() {
    this(
        new TurmaService(
            new TurmaRepository(), new PeriodoRepository(), new DisciplinaRepository()),
        new HistoricoService(new HistoricoRepository(), null, null, null, null),
        new DiarioService(new DiarioRepository(), new TurmaRepository(), new UsuarioRepository()),
        new UsuarioRepository());
  }

  public CoordenadorCLI(
      TurmaService turmaService,
      HistoricoService historicoService,
      DiarioService diarioService,
      UsuarioRepository usuarioRepository) {
    this.turmaService = turmaService;
    this.historicoService = historicoService;
    this.diarioService = diarioService;
    this.usuarioRepository = usuarioRepository;
  }

  public void processar(String input) {
    if (input == null || input.trim().isEmpty()) {
      return;
    }

    String[] partes = input.trim().split("\\s+");
    String comando = partes[0];

    try {
      Usuario logado = AutenticacaoService.getInstancia().getUsuarioLogado();
      if (logado == null || !"COORDENADOR".equalsIgnoreCase(logado.getPerfil())) {
        System.err.println(
            "ACESSO NEGADO: Apenas usuarios autenticados com o perfil de Coordenador podem executar esta acao.");
        return;
      }

      if (comando.equals("criarDiario")) {
        if (partes.length < 9) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso: criarDiario <codigoDiario> <codigoDisciplina> <periodo> <descricao> <matriculaProfessor> <horario> <sala> <cargaHoraria>");
          return;
        }

        String codigo = partes[1];
        String codigoDisciplina = partes[2];
        String periodo = partes[3];
        String descricao = partes[4];
        String matriculaProfessor = partes[5];
        String horario = partes[6];
        String sala = partes[7];
        int cargaHoraria = Integer.parseInt(partes[8]);

        diarioService.criarDiario(
            codigo,
            codigoDisciplina,
            periodo,
            descricao,
            matriculaProfessor,
            horario,
            sala,
            cargaHoraria);
        System.out.println("SUCESSO: Diário criado e associado com sucesso!");

      } else if (comando.equals("ofertarTurma")) {
        if (partes.length < 7) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso: ofertarTurma <disciplina> <professor> <periodo> <vagas> <horario> <sala>");
          return;
        }

        turmaService.ofertarTurma(
            partes[1],
            partes[2],
            partes[3],
            Integer.parseInt(partes[4]),
            partes[5],
            partes[6],
            logado.getPerfil());
        System.out.println("SUCESSO: Turma ofertada com sucesso!");

      } else if (comando.equals("editarTurma")) {
        if (partes.length < 7) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso: editarTurma <disciplina> <periodo> <novoProfessor> <novasVagas> <novoHorario> <novaSala>");
          return;
        }

        String codigoDisciplina = partes[1];
        String periodo = partes[2];
        String novoProfessor = partes[3];
        int novasVagas = Integer.parseInt(partes[4]);
        String novoHorario = partes[5];
        String novaSala = partes[6];

        turmaService.editarTurma(
            codigoDisciplina, periodo, novoProfessor, novasVagas, novoHorario, novaSala);
        System.out.println("SUCESSO: Turma editada com sucesso!");

      } else if (comando.equals("cancelarTurma")) {
        if (partes.length < 3) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso: cancelarTurma <disciplina> <periodo>");
          return;
        }
        turmaService.cancelarTurma(partes[1], partes[2]);
        System.out.println("SUCESSO: Turma cancelada com sucesso!");

      } else if (comando.equals("gerarRelatorioAlunosMatriculados")) {
        if (partes.length < 3) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso: gerarRelatorioAlunosMatriculados <codigoDisciplina> <codigoPeriodo>");
          return;
        }

        String codigoDisciplina = partes[1];
        String codigoPeriodo = partes[2];

        List<Matricula> matriculados =
            turmaService.gerarRelatorioAlunosMatriculados(codigoDisciplina, codigoPeriodo);

        System.out.println("\n=========================================================");
        System.out.println("      RELATORIO DE ALUNOS MATRICULADOS POR TURMA         ");
        System.out.println("=========================================================");
        System.out.println(
            " TURMA: " + codigoDisciplina.toUpperCase() + " | PERIODO: " + codigoPeriodo);
        System.out.println("---------------------------------------------------------");

        if (matriculados.isEmpty()) {
          System.out.println(" STATUS: Nao ha alunos matriculados nesta turma.");
        } else {
          int ordem = 1;
          for (Matricula m : matriculados) {
            System.out.println(" " + ordem + " - Matricula: " + m.getMatriculaAluno());
            ordem++;
          }
          System.out.println("---------------------------------------------------------");
          System.out.println(" TOTAL DE ALUNOS MATRICULADOS: " + matriculados.size());
        }
        System.out.println("=========================================================\n");

      } else if (comando.equalsIgnoreCase("consultarDiariosTurma")
          || comando.equalsIgnoreCase("diariosTurma")) {
        if (partes.length != 3) {
          System.err.println(
              "Erro: Uso: consultarDiariosTurma <codigo_disciplina> <codigo_periodo>");
          return;
        }

        List<br.edu.uepb.classroompb.model.Diario> diarios =
            diarioService.consultarDiariosDaTurma(logado, partes[1], partes[2]);
        System.out.println(
            "\n==========================================================================");
        System.out.println("                    DIARIOS VINCULADOS A TURMA");
        System.out.println(
            "==========================================================================");
        System.out.println(" TURMA: " + partes[1].toUpperCase() + " | PERIODO: " + partes[2]);
        System.out.println(
            "--------------------------------------------------------------------------");
        System.out.printf(
            " %-12s | %-12s | %-10s | %-14s | %-8s | %-8s%n",
            "DIARIO", "DISCIPLINA", "PERIODO", "PROFESSOR", "SALA", "STATUS");
        System.out.println(
            "--------------------------------------------------------------------------");
        if (diarios.isEmpty()) {
          System.out.println(" Nenhum diario vinculado a esta turma.");
        } else {
          System.out.print(diarioService.formatarListaDiarios(diarios));
        }
        System.out.println(
            "==========================================================================\n");

      } else if (comando.equals("gerarRelatorioOcupacaoVagas")) {
        RelatorioOcupacaoVagas relatorio;
        String codigoPeriodo = partes.length >= 2 ? partes[1] : null;

        if (codigoPeriodo == null || codigoPeriodo.isBlank()) {
          relatorio = turmaService.gerarRelatorioOcupacaoVagas();
        } else {
          relatorio = turmaService.gerarRelatorioOcupacaoVagasPorPeriodo(codigoPeriodo);
        }

        imprimirRelatorioOcupacaoVagas(relatorio, codigoPeriodo);

      } else if (comando.equals("gerarRelatorioReprovacaoPorDisciplina")
          || comando.equals("gerarRelatorioReprovacaoDisciplina")) {
        RelatorioReprovacaoDisciplina relatorio;
        String codigoDisciplina = partes.length >= 2 ? partes[1] : null;

        if (codigoDisciplina == null || codigoDisciplina.isBlank()) {
          relatorio = historicoService.gerarRelatorioReprovacaoPorDisciplina();
        } else {
          relatorio = historicoService.gerarRelatorioReprovacaoPorDisciplina(codigoDisciplina);
        }

        imprimirRelatorioReprovacaoPorDisciplina(relatorio, codigoDisciplina);

      } else if (comando.equals("exibirListaEspera")) {

        if (partes.length < 3) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso: exibirListaEspera <codigoDisciplina> <codigoPeriodo>");
          return;
        }

        String codigoDisciplina = partes[1];
        String codigoPeriodo = partes[2];

        List<Matricula> fila = turmaService.obterListaEspera(codigoDisciplina, codigoPeriodo);

        System.out.println("\n=========================================================");
        System.out.println("      📋 FILA DE ESPERA OFICIAL — COORDENAÇÃO            ");
        System.out.println("=========================================================");
        System.out.println(
            " TURMA: " + codigoDisciplina.toUpperCase() + " | PERÍODO: " + codigoPeriodo);
        System.out.println("---------------------------------------------------------");

        if (fila.isEmpty()) {
          System.out.println(" STATUS: Não há alunos aguardando na fila desta turma.");
        } else {
          int posicao = 1;
          for (Matricula m : fila) {
            System.out.println(" " + posicao + "º Lugar - Matrícula: " + m.getMatriculaAluno());
            posicao++;
          }
        }
        System.out.println("=========================================================\n");

      } else if (comando.equalsIgnoreCase("consultarHistoricoAluno")) {
        if (partes.length != 2) {
          System.err.println("Erro: Uso: consultarHistoricoAluno <matricula_aluno>");
          return;
        }

        String matriculaAluno = partes[1];
        Usuario aluno = usuarioRepository.buscarPorMatricula(matriculaAluno);
        if (aluno == null || !"ALUNO".equalsIgnoreCase(aluno.getPerfil())) {
          System.err.println("ERRO DE VALIDACAO: Aluno nao encontrado para a matricula informada.");
          return;
        }

        List<br.edu.uepb.classroompb.model.Historico> historico =
            historicoService.consultarHistoricoAluno(
                matriculaAluno, usuarioRepository, logado.getCodigoCurso());

        System.out.println(
            "\n=========================================================================================");
        System.out.println("                    HISTORICO ACADEMICO - CONSULTA DA COORDENACAO");
        System.out.println(
            "=========================================================================================");
        System.out.println(
            " MATRICULA DO ALUNO : " + aluno.getMatricula() + " | NOME: " + aluno.getNome());
        System.out.println(
            "-----------------------------------------------------------------------------------------");
        System.out.printf(
            " %-12s | %-12s | %-16s | %-7s | %-7s | %-25s%n",
            "PERIODO", "DISCIPLINA", "PROFESSOR", "MEDIA", "FREQ", "SITUACAO");
        System.out.println(
            "-----------------------------------------------------------------------------------------");

        if (historico.isEmpty()) {
          System.out.println(" Nenhum registro historico consolidado para este aluno.");
        } else {
          System.out.print(historicoService.formatarHistorico(historico));
        }
        System.out.println(
            "=========================================================================================\n");

      } else {
        System.out.println(
            "[Módulo Coordenador] Comando '" + comando + "' ainda não implementado.");
      }

    } catch (NumberFormatException e) {
      System.err.println("ERRO: O campo de carga horária/vagas deve ser um número inteiro.");
    } catch (ValidacaoException e) {
      System.err.println("ERRO DE VALIDAÇÃO: " + e.getMessage());
    } catch (ChoqueHorarioException e) {
      System.err.println("[CONFLITO DE HORÁRIO] " + e.getMessage());
    } catch (ChoqueSalaException e) {
      System.err.println("ERRO DE ALOCACAO: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      System.err.println("ERRO DE VALIDAÇÃO: " + e.getMessage());
    } catch (IllegalStateException e) {
      System.err.println("ERRO DE ESTADO: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("ERRO INTERNO: " + e.getMessage());
    }
  }

  private void imprimirRelatorioOcupacaoVagas(
      RelatorioOcupacaoVagas relatorio, String codigoPeriodo) {
    String escopo =
        codigoPeriodo == null || codigoPeriodo.isBlank()
            ? "TODOS OS PERIODOS"
            : "PERIODO: " + codigoPeriodo;

    System.out.println(
        "\n================================================================================");
    System.out.println("                 RELATORIO DE OCUPACAO DE VAGAS - RF41");
    System.out.println(
        "================================================================================");
    System.out.println(" ESCOPO: " + escopo);
    System.out.println(
        "--------------------------------------------------------------------------------");

    if (relatorio.isVazio()) {
      System.out.println(" STATUS: Nao ha turmas ofertadas para o escopo informado.");
      System.out.println(
          "================================================================================\n");
      return;
    }

    System.out.printf(
        " %-10s | %-8s | %5s | %5s | %5s | %5s | %7s | %-10s%n",
        "DISCIPLINA", "PERIODO", "TETO", "OCUP", "LIVRE", "FILA", "DENS.", "STATUS");
    System.out.println(
        "--------------------------------------------------------------------------------");

    for (OcupacaoVagasTurma ocupacao : relatorio.getOcupacoes()) {
      System.out.printf(
          " %-10s | %-8s | %5d | %5d | %5d | %5d | %6.1f%% | %-10s%n",
          ocupacao.getCodigoDisciplina(),
          ocupacao.getPeriodo(),
          ocupacao.getTetoVagas(),
          ocupacao.getVagasOcupadas(),
          ocupacao.getVagasDisponiveis(),
          ocupacao.getAlunosEmEspera(),
          ocupacao.getDensidadePercentual(),
          classificarOcupacao(ocupacao));
    }

    System.out.println(
        "--------------------------------------------------------------------------------");
    System.out.println(" TOTAL DE TURMAS              : " + relatorio.getTotalTurmas());
    System.out.println(" TETO TOTAL DE VAGAS          : " + relatorio.getTetoTotalVagas());
    System.out.println(" VAGAS OCUPADAS               : " + relatorio.getTotalVagasOcupadas());
    System.out.println(" VAGAS DISPONIVEIS            : " + relatorio.getTotalVagasDisponiveis());
    System.out.println(" ALUNOS EM LISTA DE ESPERA    : " + relatorio.getTotalAlunosEmEspera());
    System.out.println(" OCUPACAO EXCEDENTE           : " + relatorio.getTotalOcupacaoExcedente());
    System.out.println(" TURMAS LOTADAS               : " + relatorio.getTurmasLotadas());
    System.out.println(" TURMAS COM LISTA DE ESPERA   : " + relatorio.getTurmasComListaEspera());
    System.out.printf(
        " DENSIDADE GERAL              : %.1f%%%n", relatorio.getDensidadeGeralPercentual());
    System.out.printf(
        " DENSIDADE MEDIA POR TURMA    : %.1f%%%n", relatorio.getDensidadeMediaPercentual());
    System.out.println(
        "================================================================================\n");
  }

  private String classificarOcupacao(OcupacaoVagasTurma ocupacao) {
    if (ocupacao.isAcimaDoTeto()) {
      return "ACIMA";
    }
    if (ocupacao.isLotada()) {
      return "LOTADA";
    }
    if (ocupacao.isComListaEspera()) {
      return "COM_FILA";
    }
    return "COM_VAGA";
  }

  private void imprimirRelatorioReprovacaoPorDisciplina(
      RelatorioReprovacaoDisciplina relatorio, String codigoDisciplina) {
    String escopo =
        codigoDisciplina == null || codigoDisciplina.isBlank()
            ? "TODAS AS DISCIPLINAS"
            : "DISCIPLINA: " + codigoDisciplina.toUpperCase();

    System.out.println(
        "\n==========================================================================================");
    System.out.println("              RELATORIO DE REPROVACAO POR DISCIPLINA - RF42");
    System.out.println(
        "==========================================================================================");
    System.out.println(" ESCOPO: " + escopo);
    System.out.println(
        "------------------------------------------------------------------------------------------");

    if (relatorio.isVazio()) {
      System.out.println(" STATUS: Nao ha historico consolidado para o escopo informado.");
      System.out.println(
          "==========================================================================================\n");
      return;
    }

    System.out.printf(
        " %-10s | %5s | %5s | %5s | %5s | %5s | %5s | %7s%n",
        "DISCIPLINA", "TOTAL", "APROV", "RECUP", "REP_N", "REP_F", "REP_T", "TAXA");
    System.out.println(
        "------------------------------------------------------------------------------------------");

    for (ReprovacaoDisciplina indicador : relatorio.getIndicadores()) {
      System.out.printf(
          " %-10s | %5d | %5d | %5d | %5d | %5d | %5d | %6.1f%%%n",
          indicador.getCodigoDisciplina(),
          indicador.getTotalRegistros(),
          indicador.getTotalAprovados(),
          indicador.getTotalRecuperacao(),
          indicador.getTotalReprovadosPorNota(),
          indicador.getTotalReprovadosPorFalta(),
          indicador.getTotalReprovados(),
          indicador.getTaxaReprovacaoPercentual());
    }

    System.out.println(
        "------------------------------------------------------------------------------------------");
    System.out.println(" TOTAL DE DISCIPLINAS         : " + relatorio.getTotalDisciplinas());
    System.out.println(
        " REGISTROS ANALISADOS         : " + relatorio.getTotalRegistrosAnalisados());
    System.out.println(" APROVACOES                   : " + relatorio.getTotalAprovados());
    System.out.println(" RECUPERACOES                 : " + relatorio.getTotalRecuperacao());
    System.out.println(" REPROVACOES POR NOTA         : " + relatorio.getTotalReprovadosPorNota());
    System.out.println(" REPROVACOES POR FALTA        : " + relatorio.getTotalReprovadosPorFalta());
    System.out.println(" TOTAL DE REPROVACOES         : " + relatorio.getTotalReprovados());
    System.out.printf(
        " TAXA GERAL DE REPROVACAO     : %.1f%%%n", relatorio.getTaxaReprovacaoGeralPercentual());
    System.out.println(
        "==========================================================================================\n");
  }
}
