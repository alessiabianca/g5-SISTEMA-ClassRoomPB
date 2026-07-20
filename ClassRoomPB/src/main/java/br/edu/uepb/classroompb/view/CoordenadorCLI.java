package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.OcupacaoVagasTurma;
import br.edu.uepb.classroompb.model.RelatorioOcupacaoVagas;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.DisciplinaRepository;
import br.edu.uepb.classroompb.repository.HistoricoRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.HistoricoService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ChoqueSalaException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.List;

public class CoordenadorCLI {
  private final TurmaService turmaService;
  private final HistoricoService historicoService;
  private final UsuarioRepository usuarioRepository;

  public CoordenadorCLI() {
    this(
        new TurmaService(
            new TurmaRepository(), new PeriodoRepository(), new DisciplinaRepository()),
        new HistoricoService(new HistoricoRepository(), null, null, null, null),
        new UsuarioRepository());
  }

  public CoordenadorCLI(
      TurmaService turmaService,
      HistoricoService historicoService,
      UsuarioRepository usuarioRepository) {
    this.turmaService = turmaService;
    this.historicoService = historicoService;
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

      if (comando.equals("ofertarTurma")) {
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

      } else if (comando.equals("gerarRelatorioOcupacaoVagas")) {
        RelatorioOcupacaoVagas relatorio;
        String codigoPeriodo = partes.length >= 2 ? partes[1] : null;

        if (codigoPeriodo == null || codigoPeriodo.isBlank()) {
          relatorio = turmaService.gerarRelatorioOcupacaoVagas();
        } else {
          relatorio = turmaService.gerarRelatorioOcupacaoVagasPorPeriodo(codigoPeriodo);
        }

        imprimirRelatorioOcupacaoVagas(relatorio, codigoPeriodo);

      } else if (comando.equals("exibirListaEspera")) {
        // [TASK 2283] Mapeamento do comando de visualização da lista de espera
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
      System.err.println("ERRO: O campo vagas deve ser um número inteiro.");
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
}
