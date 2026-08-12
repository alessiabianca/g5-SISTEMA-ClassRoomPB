package br.edu.uepb.classroompb.view;

import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.service.AutenticacaoService;
import br.edu.uepb.classroompb.service.DiarioService;
import br.edu.uepb.classroompb.service.FrequenciaService;
import br.edu.uepb.classroompb.service.NotaService;
import br.edu.uepb.classroompb.service.TurmaService;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProfessorCLI {
  private final TurmaService turmaService;
  private final FrequenciaService frequenciaService;
  private final NotaService notaService;
  private final br.edu.uepb.classroompb.service.AvaliacaoService avaliacaoService;
  private final DiarioService diarioService;
  private final AutenticacaoService authService = AutenticacaoService.getInstancia();

  public ProfessorCLI(
      TurmaService turmaService,
      FrequenciaService frequenciaService,
      NotaService notaService,
      br.edu.uepb.classroompb.service.AvaliacaoService avaliacaoService) {
    this(
        turmaService,
        frequenciaService,
        notaService,
        avaliacaoService,
        new DiarioService(
            new br.edu.uepb.classroompb.repository.DiarioRepository(),
            new br.edu.uepb.classroompb.repository.TurmaRepository(),
            new br.edu.uepb.classroompb.repository.UsuarioRepository()));
  }

  public ProfessorCLI(
      TurmaService turmaService,
      FrequenciaService frequenciaService,
      NotaService notaService,
      br.edu.uepb.classroompb.service.AvaliacaoService avaliacaoService,
      DiarioService diarioService) {
    this.turmaService = turmaService;
    this.frequenciaService = frequenciaService;
    this.notaService = notaService;
    this.avaliacaoService = avaliacaoService;
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
      if (logado == null || !"PROFESSOR".equalsIgnoreCase(logado.getPerfil())) {
        System.err.println(
            "ACESSO NEGADO: Apenas professores autenticados podem executar ações neste módulo.");
        return;
      }

      if (comando.equalsIgnoreCase("lancarNota")) {
        if (partes.length < 4) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso correto: lancarNota [matricula_aluno] [id_avaliacao] [nota]");
          return;
        }

        String matriculaAluno = partes[1];
        String idAvaliacao = partes[2];
        double valorNota;

        try {
          valorNota = Double.parseDouble(partes[3]);
        } catch (NumberFormatException e) {
          System.err.println("Erro: A Nota deve ser um valor numérico válido.");
          return;
        }

        String matriculaProfessor = logado.getMatricula();

        notaService.lancarNota(matriculaProfessor, matriculaAluno, idAvaliacao, valorNota);

        System.out.println("\n=========================================================");
        System.out.println("            🧾 COMPROVANTE DE LANÇAMENTO DE NOTA         ");
        System.out.println("=========================================================");
        System.out.println(" STATUS DO REGISTRO : ✅ HOMOLOGADO E PUBLICADO");
        System.out.println(" ALUNO AVALIADO     : " + matriculaAluno);
        System.out.println(" AVALIAÇÃO (ID)     : " + idAvaliacao);
        System.out.println(" VALOR ATRIBUÍDO    : ⭐ " + String.format("%.1f", valorNota));
        System.out.println("---------------------------------------------------------");
        System.out.println(" Sistema ClassRoomPB - Registro seguro de desempenho.");
        System.out.println("=========================================================");

      } else if (comando.equalsIgnoreCase("editarNota")) {
        if (partes.length < 4) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso correto: editarNota [matricula_aluno] [id_avaliacao] [novo_valor]");
          return;
        }

        String matriculaAluno = partes[1];
        String idAvaliacao = partes[2];
        double novoValor;

        try {
          novoValor = Double.parseDouble(partes[3]);
        } catch (NumberFormatException e) {
          System.err.println("Erro: A Nota deve ser um valor numérico válido.");
          return;
        }

        String matriculaProfessor = logado.getMatricula();

        notaService.retificarNota(matriculaProfessor, matriculaAluno, idAvaliacao, novoValor);

        System.out.println("\n=========================================================");
        System.out.println("          🔄 COMPROVANTE DE RETIFICAÇÃO DE NOTA          ");
        System.out.println("=========================================================");
        System.out.println(" STATUS DO REGISTRO : ✅ RETIFICAÇÃO HOMOLOGADA");
        System.out.println(" ALUNO RETIFICADO   : " + matriculaAluno);
        System.out.println(" AVALIAÇÃO (ID)     : " + idAvaliacao);
        System.out.println(" NOVO VALOR         : ⭐ " + String.format("%.1f", novoValor));
        System.out.println("---------------------------------------------------------");
        System.out.println(" Sistema ClassRoomPB - Retificação segura de desempenho.");
        System.out.println("=========================================================\n");

      } else if (comando.equalsIgnoreCase("cadastrarAvaliacao")) {
        if (partes.length < 6) {
          System.err.println(
              "Uso: cadastrarAvaliacao [codigo_diario] [descricao_sem_espaco] [etapa] [peso] [nota_maxima]");
          return;
        }

        String matriculaProfessor = logado.getMatricula();
        String codigoDiario = partes[1];
        String descricao = partes[2].replace("_", " ");
        int etapa = Integer.parseInt(partes[3]);
        double peso = Double.parseDouble(partes[4]);
        double notaMaxima = Double.parseDouble(partes[5]);

        br.edu.uepb.classroompb.model.Avaliacao avaliacao =
            avaliacaoService.cadastrarAvaliacao(
                matriculaProfessor, codigoDiario, descricao, etapa, peso, notaMaxima);

        System.out.println("Sucesso: Avaliação cadastrada com ID: " + avaliacao.getId());

      } else if (comando.equalsIgnoreCase("fecharDiario")) {
        if (partes.length != 2) {
          System.err.println("Uso: fecharDiario [codigo_diario]");
          return;
        }

        br.edu.uepb.classroompb.model.Diario diario =
            diarioService.fecharDiario(logado.getMatricula(), partes[1]);
        System.out.println(
            "Sucesso: Diario '" + diario.getCodigo() + "' fechado e bloqueado para alteracoes.");

      } else if (comando.equalsIgnoreCase("meusDiarios")) {
        List<br.edu.uepb.classroompb.model.Diario> diarios =
            diarioService.consultarMeusDiarios(logado);
        System.out.println(
            "\n==========================================================================");
        System.out.println("                         MEUS DIARIOS");
        System.out.println(
            "==========================================================================");
        System.out.printf(
            " %-12s | %-12s | %-10s | %-14s | %-8s | %-8s%n",
            "DIARIO", "DISCIPLINA", "PERIODO", "PROFESSOR", "SALA", "STATUS");
        System.out.println(
            "--------------------------------------------------------------------------");
        if (diarios.isEmpty()) {
          System.out.println(" Nenhum diario sob sua responsabilidade.");
        } else {
          System.out.print(diarioService.formatarListaDiarios(diarios));
        }
        System.out.println(
            "==========================================================================\n");

      } else if (comando.equalsIgnoreCase("consultarMeuDiario")) {
        if (partes.length != 2) {
          System.err.println("Uso: consultarMeuDiario [codigo_diario]");
          return;
        }
        br.edu.uepb.classroompb.model.Diario diario =
            diarioService.consultarDiarioDoProfessor(logado, partes[1]);
        System.out.print(diarioService.formatarListaDiarios(List.of(diario)));

      } else if (comando.equalsIgnoreCase("registrarChamada")) {
        if (partes.length < 3) {
          System.err.println(
              "Erro: Parâmetros insuficientes. Uso correto: registrarChamada [codigo_diario] [id_aula]");
          return;
        }

        String codigoDiario = partes[1];
        String idAula = partes[2];
        String matriculaProfessor = logado.getMatricula();

        br.edu.uepb.classroompb.model.Diario diario =
            diarioService.consultarDiarioDoProfessor(logado, codigoDiario);

        String codigoDisciplina = diario.getCodigoDisciplina();
        String codigoPeriodo = diario.getPeriodo();

        br.edu.uepb.classroompb.repository.MatriculaRepository mRepo =
            new br.edu.uepb.classroompb.repository.MatriculaRepository();
        List<Matricula> todasMatriculas = mRepo.buscarTodas();
        List<Matricula> matriculasDaTurma = new ArrayList<>();

        for (Matricula m : todasMatriculas) {
          if (m.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
              && m.getPeriodo().equalsIgnoreCase(codigoPeriodo)
              && m.getStatus() == Matricula.StatusMatricula.CONFIRMADA) {
            matriculasDaTurma.add(m);
          }
        }

        if (matriculasDaTurma.isEmpty()) {
          System.out.println(
              "\n Não há alunos com matrícula CONFIRMADA nesta turma para registrar chamada.\n");
          return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=========================================================");
        System.out.println("          📋 INICIANDO DIÁRIO DE CLASSE ITERATIVO        ");
        System.out.println("=========================================================");
        System.out.println(" DIÁRIO : " + codigoDiario + " | AULA: " + idAula);
        System.out.println("---------------------------------------------------------");
        System.out.println(" Digite 'P' para PRESENÇA ou 'F' para FALTA para cada aluno:");
        System.out.println("---------------------------------------------------------");

        List<Matricula> loteParaSalvar = new ArrayList<>();

        for (Matricula matriculaAluno : matriculasDaTurma) {
          while (true) {
            System.out.print(" Aluno: " + matriculaAluno.getMatriculaAluno() + " [P/F]: ");
            String entrada = scanner.nextLine().trim().toUpperCase();

            if (entrada.equals("P")) {
              loteParaSalvar.add(
                  new Matricula(
                      matriculaAluno.getMatriculaAluno(),
                      matriculaAluno.getCodigoDisciplina(),
                      matriculaAluno.getPeriodo(),
                      Matricula.StatusMatricula.CONFIRMADA));
              break;
            } else if (entrada.equals("F")) {
              loteParaSalvar.add(
                  new Matricula(
                      matriculaAluno.getMatriculaAluno(),
                      matriculaAluno.getCodigoDisciplina(),
                      matriculaAluno.getPeriodo(),
                      Matricula.StatusMatricula.SOLICITADA));
              break;
            } else {
              System.out.println("   ❌ Opção inválida! Digite apenas 'P' ou 'F'.");
            }
          }
        }

        frequenciaService.registrarChamadaLote(
            matriculaProfessor, codigoDiario, idAula, loteParaSalvar);

        System.out.println("\n=========================================================");
        System.out.println("           🧾 DIÁRIO DE CLASSE FECHADO COM SUCESSO       ");
        System.out.println("=========================================================");
        System.out.println(" STATUS DA CHAMADA  : ✅ HOMOLOGADA E SALVA EM DISCO");
        System.out.println(" DIÁRIO / AULA      : " + codigoDiario + " / " + idAula);
        System.out.println(" TOTAL DE ALUNOS    : " + loteParaSalvar.size() + " avaliados.");
        System.out.println("---------------------------------------------------------");
        System.out.println(" Registrado por: Prof. " + matriculaProfessor);
        System.out.println("=========================================================\n");

      } else {
        System.out.println(
            "Erro: Comando '" + comando + "' não reconhecido no Módulo do Professor.");
      }

    } catch (ValidacaoException e) {
      System.out.println("\n---------------------------------------------------------");
      System.out.println("          ⚠️ OPERAÇÃO RECUSADA PELO SISTEMA ⚠️");
      System.out.println("---------------------------------------------------------");
      System.out.println(e.getMessage());
      System.out.println("---------------------------------------------------------\n");

    } catch (Exception e) {
      System.err.println("ERRO INTERNO NO MÓDULO DO PROFESSOR: " + e.getMessage());
    }
  }

  private class RegistroChamadaDTO {
    String matriculaAluno;
    String status;

    RegistroChamadaDTO(String matriculaAluno, String status) {
      this.matriculaAluno = matriculaAluno;
      this.status = status;
    }
  }
}
