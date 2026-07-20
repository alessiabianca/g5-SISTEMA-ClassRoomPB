package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.RelatorioUsuariosCadastrados;
import br.edu.uepb.classroompb.model.UsuarioCadastradoResumo;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.PeriodoService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ChoqueSalaException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;

public class AdminCLI {
  private final PeriodoService periodoService;
  private final TurmaService turmaService;

  public AdminCLI(PeriodoService periodoService, TurmaService turmaService) {
    this.periodoService = periodoService;
    this.turmaService = turmaService;
  }

  public void processar(String input) {
    if (input == null || input.trim().isEmpty()) {
      return;
    }

    String[] partes = input.trim().split("\\s+");
    String comando = partes[0];

    switch (comando) {
      case "cadastrarPeriodo":
        if (partes.length < 2) {
          System.err.println("Erro: Uso correto: cadastrarPeriodo <codigo>");
          return;
        }
        try {
          periodoService.cadastrarPeriodo(partes[1]);
          System.out.println("Sucesso: Periodo " + partes[1] + " cadastrado como PLANEJADO.");
        } catch (ValidacaoException e) {
          System.err.println("Erro de Validacao: " + e.getMessage());
        }
        break;

      case "ativarPeriodo":
        if (partes.length < 2) {
          System.err.println("Erro: Uso correto: ativarPeriodo <codigo>");
          return;
        }
        try {
          periodoService.activarPeriodo(partes[1]);
          System.out.println("Sucesso: Periodo " + partes[1] + " agora esta INICIADO.");
        } catch (ValidacaoException e) {
          System.err.println("Erro de Validacao: " + e.getMessage());
        }
        break;

      case "encerrarPeriodo":
        if (partes.length < 2) {
          System.err.println("Erro: Uso correto: encerrarPeriodo <codigo>");
          return;
        }
        try {
          periodoService.encerrarPeriodo(partes[1]);
          System.out.println("Sucesso: Periodo " + partes[1] + " agora esta ENCERRADO.");
        } catch (ValidacaoException e) {
          System.err.println("Erro de Validacao: " + e.getMessage());
        }
        break;

      case "ofertarTurma":
        if (partes.length < 7) {
          System.err.println(
              "Erro: Uso correto: ofertarTurma <codigoDisciplina> <professor> <periodo> <vagas> <horario> <sala>");
          return;
        }
        try {
          String codigoDisciplina = partes[1];
          String matriculaProfessor = partes[2];
          String periodo = partes[3];
          int vagas = Integer.parseInt(partes[4]);
          String horario = partes[5];
          String sala = partes[6];

          turmaService.ofertarTurma(
              codigoDisciplina, matriculaProfessor, periodo, vagas, horario, sala, "ADMINISTRADOR");
          System.out.println(
              "Sucesso: Turma de "
                  + codigoDisciplina
                  + " ofertada com sucesso para o periodo "
                  + periodo
                  + ".");
        } catch (NumberFormatException e) {
          System.err.println("Erro de Formato: O campo 'vagas' deve ser um numero inteiro valido.");
        } catch (ChoqueHorarioException | ChoqueSalaException e) {
          System.err.println("Erro de Alocacao: " + e.getMessage());
        } catch (ValidacaoException e) {
          System.err.println("Erro de Validacao: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
          System.err.println("Erro de Negocio: " + e.getMessage());
        }
        break;

      case "gerarRelatorioGeralUsuariosCadastrados":
      case "gerarRelatorioUsuariosCadastrados":
        try {
          RelatorioUsuariosCadastrados relatorio =
              AutenticacaoService.getInstancia().gerarRelatorioGeralUsuariosCadastrados();
          imprimirRelatorioGeralUsuariosCadastrados(relatorio);
        } catch (ValidacaoException e) {
          System.err.println("Erro de Validacao: " + e.getMessage());
        }
        break;

      default:
        System.out.println(
            "[Módulo Administrador] Comando recebido: "
                + comando
                + " (Funcionalidade em desenvolvimento na US correspondente)");
        break;
    }
  }

  private void imprimirRelatorioGeralUsuariosCadastrados(RelatorioUsuariosCadastrados relatorio) {
    System.out.println(
        "\n==========================================================================================");
    System.out.println("             RELATORIO GERAL DE USUARIOS CADASTRADOS - RF43");
    System.out.println(
        "==========================================================================================");

    if (relatorio.isVazio()) {
      System.out.println(" STATUS: Nao ha usuarios cadastrados no sistema.");
      System.out.println(
          "==========================================================================================\n");
      return;
    }

    System.out.printf(
        " %-14s | %-14s | %-24s | %-30s | %-14s%n",
        "MATRICULA", "PERFIL", "NOME", "EMAIL", "CURSO");
    System.out.println(
        "------------------------------------------------------------------------------------------");

    for (UsuarioCadastradoResumo usuario : relatorio.getUsuarios()) {
      System.out.printf(
          " %-14s | %-14s | %-24s | %-30s | %-14s%n",
          usuario.getMatricula(),
          usuario.getPerfil(),
          usuario.getNome(),
          usuario.getEmail(),
          usuario.getCodigoCurso());
    }

    System.out.println(
        "------------------------------------------------------------------------------------------");
    System.out.println(" TOTAL DE USUARIOS             : " + relatorio.getTotalUsuarios());
    System.out.println(" ALUNOS                        : " + relatorio.getTotalAlunos());
    System.out.println(" PROFESSORES                   : " + relatorio.getTotalProfessores());
    System.out.println(" COORDENADORES                 : " + relatorio.getTotalCoordenadores());
    System.out.println(" ADMINISTRADORES               : " + relatorio.getTotalAdministradores());
    System.out.println(
        " USUARIOS COM CURSO VINCULADO  : " + relatorio.getTotalUsuariosComCursoVinculado());
    System.out.println(
        " USUARIOS SEM CURSO VINCULADO  : " + relatorio.getTotalUsuariosSemCursoVinculado());
    System.out.println(
        "==========================================================================================\n");
  }
}
