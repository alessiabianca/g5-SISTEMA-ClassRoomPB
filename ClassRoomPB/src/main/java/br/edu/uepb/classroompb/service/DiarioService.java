package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Aula;
import br.edu.uepb.classroompb.model.Avaliacao;
import br.edu.uepb.classroompb.model.Diario;
import br.edu.uepb.classroompb.model.ExtratoDiarioAluno;
import br.edu.uepb.classroompb.model.Frequencia;
import br.edu.uepb.classroompb.model.Matricula;
import br.edu.uepb.classroompb.model.Nota;
import br.edu.uepb.classroompb.model.NotaAvaliacaoDiario;
import br.edu.uepb.classroompb.model.Professor;
import br.edu.uepb.classroompb.model.Turma;
import br.edu.uepb.classroompb.model.Usuario;
import br.edu.uepb.classroompb.repository.AulaRepository;
import br.edu.uepb.classroompb.repository.AvaliacaoRepository;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.FrequenciaRepository;
import br.edu.uepb.classroompb.repository.MatriculaRepository;
import br.edu.uepb.classroompb.repository.NotaRepository;
import br.edu.uepb.classroompb.repository.TurmaRepository;
import br.edu.uepb.classroompb.repository.UsuarioRepository;
import br.edu.uepb.classroompb.service.exception.ChoqueHorarioException;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DiarioService {

  private final DiarioRepository diarioRepository;
  private final TurmaRepository turmaRepository;
  private final UsuarioRepository usuarioRepository;
  private final AulaRepository aulaRepository;
  private final AvaliacaoRepository avaliacaoRepository;
  private final MatriculaRepository matriculaRepository;
  private final FrequenciaRepository frequenciaRepository;
  private final NotaRepository notaRepository;

  public DiarioService(
      DiarioRepository diarioRepository,
      TurmaRepository turmaRepository,
      UsuarioRepository usuarioRepository) {
    this(
        diarioRepository,
        turmaRepository,
        usuarioRepository,
        new AulaRepository(),
        new AvaliacaoRepository(),
        new MatriculaRepository(),
        new FrequenciaRepository(),
        new NotaRepository());
  }

  public DiarioService(
      DiarioRepository diarioRepository,
      TurmaRepository turmaRepository,
      UsuarioRepository usuarioRepository,
      AulaRepository aulaRepository,
      AvaliacaoRepository avaliacaoRepository,
      MatriculaRepository matriculaRepository,
      FrequenciaRepository frequenciaRepository,
      NotaRepository notaRepository) {
    if (diarioRepository == null
        || turmaRepository == null
        || usuarioRepository == null
        || aulaRepository == null
        || avaliacaoRepository == null
        || matriculaRepository == null
        || frequenciaRepository == null
        || notaRepository == null) {
      throw new IllegalArgumentException("Erro: Repositórios não podem ser nulos.");
    }
    this.diarioRepository = diarioRepository;
    this.turmaRepository = turmaRepository;
    this.usuarioRepository = usuarioRepository;
    this.aulaRepository = aulaRepository;
    this.avaliacaoRepository = avaliacaoRepository;
    this.matriculaRepository = matriculaRepository;
    this.frequenciaRepository = frequenciaRepository;
    this.notaRepository = notaRepository;
  }

  /**
   * TASK 2790 e TASK 2793: Criação de Diário com validação de turma, professor responsável (RN18) e
   * bloqueio de choque de horário de professores cruzando diários ativos (RF12).
   */
  public Diario criarDiario(
      String codigo,
      String codigoDisciplina,
      String periodo,
      String descricao,
      String matriculaProfessor,
      String horario,
      String sala,
      int cargaHoraria)
      throws ValidacaoException, ChoqueHorarioException {

    // 1. Validação de campos obrigatórios
    validarCamposObrigatorios(
        codigo, codigoDisciplina, periodo, matriculaProfessor, horario, sala, cargaHoraria);

    // 2. Validação da existência da turma ofertada para o período
    validarExistenciaTurma(codigoDisciplina, periodo);

    // 3. Validação do professor (RN18: Proibido diário órfão)
    validarProfessorExistente(matriculaProfessor);

    // 4. TASK 2793: Validação de choque de horário do professor cruzando diários ativos (RF12)
    validarChoqueHorarioProfessor(matriculaProfessor, periodo, horario);

    // 5. Validação de diário duplicado por código
    if (diarioRepository.buscarPorCodigo(codigo) != null) {
      throw new ValidacaoException(
          "Erro: Já existe um diário cadastrado com o código '" + codigo + "'.");
    }

    // 6. Instanciação e Persistência
    Diario novoDiario =
        new Diario(
            codigo,
            codigoDisciplina,
            periodo,
            descricao,
            matriculaProfessor,
            horario,
            sala,
            cargaHoraria);

    diarioRepository.salvar(novoDiario);
    return novoDiario;
  }

  private void validarCamposObrigatorios(
      String codigo,
      String codigoDisciplina,
      String periodo,
      String matriculaProfessor,
      String horario,
      String sala,
      int cargaHoraria) {

    if (codigo == null
        || codigo.trim().isEmpty()
        || codigoDisciplina == null
        || codigoDisciplina.trim().isEmpty()
        || periodo == null
        || periodo.trim().isEmpty()
        || matriculaProfessor == null
        || matriculaProfessor.trim().isEmpty()
        || horario == null
        || horario.trim().isEmpty()
        || sala == null
        || sala.trim().isEmpty()) {
      throw new IllegalArgumentException("Erro: Todos os campos do diário são obrigatórios.");
    }

    if (cargaHoraria <= 0) {
      throw new IllegalArgumentException("Erro: A carga horária deve ser maior que zero.");
    }
  }

  private void validarExistenciaTurma(String codigoDisciplina, String periodo)
      throws ValidacaoException {
    List<Turma> turmas = turmaRepository.buscarTodas();
    boolean existe = false;

    if (turmas != null) {
      for (Turma t : turmas) {
        if (t.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
            && t.getPeriodo().equalsIgnoreCase(periodo)) {
          existe = true;
          break;
        }
      }
    }

    if (!existe) {
      throw new ValidacaoException(
          "Erro: A turma '"
              + codigoDisciplina
              + "' para o período '"
              + periodo
              + "' não está ofertada.");
    }
  }

  private void validarProfessorExistente(String matriculaProfessor) throws ValidacaoException {
    Usuario usuario = usuarioRepository.buscarPorMatricula(matriculaProfessor);

    if (usuario == null || !(usuario instanceof Professor)) {
      throw new ValidacaoException(
          "Erro (RN18): O professor de matrícula '"
              + matriculaProfessor
              + "' não foi encontrado ou não possui o perfil de Professor.");
    }
  }

  /**
   * TASK 2793 / RF12: Valida se o professor já possui outro diário alocado no mesmo período e
   * horário.
   */
  private void validarChoqueHorarioProfessor(
      String matriculaProfessor, String periodo, String horario) throws ChoqueHorarioException {

    List<Diario> diariosExistentes = diarioRepository.buscarTodos();

    if (diariosExistentes != null) {
      for (Diario d : diariosExistentes) {
        if (d.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)
            && d.getPeriodo().equalsIgnoreCase(periodo)
            && d.getHorario().equalsIgnoreCase(horario)) {
          throw new ChoqueHorarioException(
              "Erro (RF12): O professor '"
                  + matriculaProfessor
                  + "' já possui o diário '"
                  + d.getCodigo()
                  + "' cadastrado no mesmo horário ("
                  + horario
                  + ") para o período '"
                  + periodo
                  + "'.");
        }
      }
    }
  }

  public List<Diario> listarTodos() {
    return diarioRepository.buscarTodos();
  }

  /** RF51: Coordenadores podem consultar todos os diarios da turma selecionada. */
  public List<Diario> consultarDiariosDaTurma(
      Usuario solicitante, String codigoDisciplina, String periodo) throws ValidacaoException {
    validarPerfil(solicitante, "COORDENADOR");
    validarTurmaExistente(codigoDisciplina, periodo);

    List<Diario> resultado = new ArrayList<>();
    for (Diario diario : diarioRepository.buscarTodos()) {
      if (diario.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && diario.getPeriodo().equalsIgnoreCase(periodo)) {
        resultado.add(diario);
      }
    }
    ordenarDiarios(resultado);
    return resultado;
  }

  /** RF51: A identidade do professor autenticado define o filtro e nao pode ser sobrescrita. */
  public List<Diario> consultarMeusDiarios(Usuario solicitante) throws ValidacaoException {
    validarPerfil(solicitante, "PROFESSOR");
    List<Diario> resultado = new ArrayList<>();
    for (Diario diario : diarioRepository.buscarTodos()) {
      if (diario.getMatriculaProfessor().equalsIgnoreCase(solicitante.getMatricula())) {
        resultado.add(diario);
      }
    }
    ordenarDiarios(resultado);
    return resultado;
  }

  public Diario consultarDiarioDoProfessor(Usuario solicitante, String codigoDiario)
      throws ValidacaoException {
    validarPerfil(solicitante, "PROFESSOR");
    Diario diario = buscarDiarioObrigatorio(codigoDiario);
    if (!diario.getMatriculaProfessor().equalsIgnoreCase(solicitante.getMatricula())) {
      throw new ValidacaoException(
          "Acesso negado: o diario nao esta sob responsabilidade do professor autenticado.");
    }
    return diario;
  }

  /** RF51: Lista somente diarios das turmas em que o aluno possui matricula confirmada. */
  public List<Diario> consultarDiariosDoAluno(Usuario solicitante) throws ValidacaoException {
    validarPerfil(solicitante, "ALUNO");
    return buscarDiariosDoAluno(solicitante.getMatricula());
  }

  /** RF51: Retorna a visao completa de cada diario vinculado ao aluno autenticado. */
  public List<ExtratoDiarioAluno> consultarExtratosDosDiariosDoAluno(Usuario solicitante)
      throws ValidacaoException {
    validarPerfil(solicitante, "ALUNO");
    List<ExtratoDiarioAluno> extratos = new ArrayList<>();
    for (Diario diario : buscarDiariosDoAluno(solicitante.getMatricula())) {
      Matricula matricula = buscarMatriculaConfirmada(solicitante.getMatricula(), diario);
      extratos.add(montarExtratoDoAluno(matricula, diario));
    }
    return extratos;
  }

  private List<Diario> buscarDiariosDoAluno(String matriculaAluno) {
    List<Diario> resultado = new ArrayList<>();
    for (Diario diario : diarioRepository.buscarTodos()) {
      if (alunoMatriculadoNoDiario(matriculaAluno, diario)) {
        resultado.add(diario);
      }
    }
    ordenarDiarios(resultado);
    return resultado;
  }

  /**
   * Retorna frequencias e notas exclusivamente do aluno autenticado, sem expor dados da pauta de
   * colegas.
   */
  public ExtratoDiarioAluno consultarExtratoDoAluno(Usuario solicitante, String codigoDiario)
      throws ValidacaoException {
    validarPerfil(solicitante, "ALUNO");
    Diario diario = buscarDiarioObrigatorio(codigoDiario);
    String matriculaAluno = solicitante.getMatricula();
    Matricula matricula = buscarMatriculaConfirmada(matriculaAluno, diario);
    if (matricula == null) {
      throw new ValidacaoException(
          "Acesso negado: o aluno nao possui matricula confirmada na turma deste diario.");
    }

    return montarExtratoDoAluno(matricula, diario);
  }

  private ExtratoDiarioAluno montarExtratoDoAluno(Matricula matricula, Diario diario) {
    String codigoDiario = diario.getCodigo();
    String matriculaAluno = matricula.getMatriculaAluno();

    List<Aula> aulas = aulaRepository.buscarPorDiario(codigoDiario);
    aulas.sort(Comparator.comparing(Aula::getData).thenComparing(Aula::getId));

    List<Frequencia> frequencias = new ArrayList<>();
    for (Frequencia frequencia : frequenciaRepository.buscarTodas()) {
      if (frequencia.getCodigoDiario().equalsIgnoreCase(codigoDiario)
          && frequencia.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)) {
        frequencias.add(frequencia);
      }
    }
    frequencias.sort(
        Comparator.comparing(Frequencia::getDataAula).thenComparing(Frequencia::getIdAula));

    Nota nota = buscarNotaDoDiario(notaRepository.buscarTodas(), diario, matriculaAluno);
    List<NotaAvaliacaoDiario> notasAvaliacoes = new ArrayList<>();
    double somaPonderada = 0.0;
    double somaPesos = 0.0;
    List<Avaliacao> avaliacoes = avaliacaoRepository.buscarPorDiario(codigoDiario);
    avaliacoes.sort(Comparator.comparingInt(Avaliacao::getEtapa).thenComparing(Avaliacao::getId));
    for (Avaliacao avaliacao : avaliacoes) {
      Double valor = obterNotaLancada(nota, avaliacao.getEtapa());
      notasAvaliacoes.add(new NotaAvaliacaoDiario(avaliacao, valor));
      if (valor != null && avaliacao.getNotaMaxima() > 0.0 && avaliacao.getPeso() > 0.0) {
        somaPonderada += (valor / avaliacao.getNotaMaxima()) * 10.0 * avaliacao.getPeso();
        somaPesos += avaliacao.getPeso();
      }
    }
    double mediaParcial = somaPesos == 0.0 ? 0.0 : somaPonderada / somaPesos;

    return new ExtratoDiarioAluno(
        matricula, diario, aulas, frequencias, notasAvaliacoes, mediaParcial);
  }

  public String formatarListaDiarios(List<Diario> diarios) {
    StringBuilder texto = new StringBuilder();
    for (Diario diario : diarios) {
      texto.append(
          String.format(
              Locale.US,
              " %-12s | %-12s | %-10s | %-14s | %-8s | %-8s%n",
              diario.getCodigo(),
              diario.getCodigoDisciplina(),
              diario.getPeriodo(),
              diario.getMatriculaProfessor(),
              diario.getSala(),
              diario.getSituacao().name()));
    }
    return texto.toString();
  }

  public String formatarExtratoAluno(ExtratoDiarioAluno extrato) {
    StringBuilder texto = new StringBuilder();
    texto.append("DIARIO: ").append(extrato.getDiario().getCodigo()).append(System.lineSeparator());
    texto
        .append("PAUTA (VISAO INDIVIDUAL): ")
        .append(extrato.getMatriculaAluno())
        .append(" | ")
        .append(extrato.getMatricula().getStatus().name())
        .append(System.lineSeparator());
    texto
        .append("DISCIPLINA: ")
        .append(extrato.getDiario().getCodigoDisciplina())
        .append(" | PERIODO: ")
        .append(extrato.getDiario().getPeriodo())
        .append(System.lineSeparator());
    texto.append("AULAS E FREQUENCIA:").append(System.lineSeparator());
    for (Aula aula : extrato.getAulas()) {
      Frequencia frequencia = buscarFrequenciaDaAula(extrato.getFrequencias(), aula.getId());
      texto
          .append(" - ")
          .append(aula.getData())
          .append(" | ")
          .append(aula.getAssunto())
          .append(" | ")
          .append(frequencia == null ? "NAO_LANCADA" : frequencia.getStatus().name())
          .append(System.lineSeparator());
    }
    texto.append("AVALIACOES E NOTAS:").append(System.lineSeparator());
    for (NotaAvaliacaoDiario item : extrato.getNotasAvaliacoes()) {
      texto
          .append(" - ")
          .append(item.getAvaliacao().getDescricao())
          .append(" (etapa ")
          .append(item.getAvaliacao().getEtapa())
          .append("): ")
          .append(item.isLancada() ? String.format(Locale.US, "%.1f", item.getValor()) : "PENDENTE")
          .append(System.lineSeparator());
    }
    texto.append(String.format(Locale.US, "MEDIA PARCIAL: %.1f%n", extrato.getMediaParcial()));
    return texto.toString();
  }

  /**
   * Fecha definitivamente um diario depois de comprovar que cada aluno da pauta possui frequencia
   * em todas as aulas e nota em todas as avaliacoes cadastradas.
   */
  public Diario fecharDiario(String matriculaProfessor, String codigoDiario)
      throws ValidacaoException {
    Diario diario = diarioRepository.buscarPorCodigo(codigoDiario);
    if (diario == null) {
      throw new ValidacaoException("Erro: Diario '" + codigoDiario + "' nao encontrado.");
    }
    if (!diario.getMatriculaProfessor().equalsIgnoreCase(matriculaProfessor)) {
      throw new ValidacaoException("Erro: Apenas o professor responsavel pode fechar este diario.");
    }
    if (diario.isFechado()) {
      throw new ValidacaoException("Erro: O diario ja esta FECHADO.");
    }

    List<Matricula> pauta = buscarPauta(diario);
    List<Aula> aulas = aulaRepository.buscarPorDiario(codigoDiario);
    List<Avaliacao> avaliacoes = avaliacaoRepository.buscarPorDiario(codigoDiario);
    List<Frequencia> frequencias = frequenciaRepository.buscarTodas();
    List<Nota> notas = notaRepository.buscarTodas();
    List<String> pendencias = new java.util.ArrayList<>();

    for (Matricula matricula : pauta) {
      String aluno = matricula.getMatriculaAluno();
      for (Aula aula : aulas) {
        if (!possuiFrequencia(frequencias, codigoDiario, aula.getId(), aluno)) {
          pendencias.add("frequencia da aula " + aula.getId() + " para o aluno " + aluno);
        }
      }

      Nota nota = buscarNotaDoDiario(notas, diario, aluno);
      for (Avaliacao avaliacao : avaliacoes) {
        if (nota == null || !possuiNotaDaEtapa(nota, avaliacao.getEtapa())) {
          pendencias.add("nota da avaliacao " + avaliacao.getId() + " para o aluno " + aluno);
        }
      }
    }

    if (!pendencias.isEmpty()) {
      throw new ValidacaoException(
          "Erro: O diario possui lancamentos pendentes: " + String.join(", ", pendencias) + ".");
    }

    List<Diario> diarios = diarioRepository.buscarTodos();
    for (Diario atual : diarios) {
      if (atual.getCodigo().equalsIgnoreCase(codigoDiario)) {
        atual.setSituacao(Diario.SituacaoDiario.FECHADO);
        diario = atual;
        break;
      }
    }
    diarioRepository.atualizarArquivoCompleto(diarios);
    return diario;
  }

  public void validarDiarioAberto(String codigoDiario) throws ValidacaoException {
    Diario diario = diarioRepository.buscarPorCodigo(codigoDiario);
    if (diario == null) {
      throw new ValidacaoException("Erro: Diario '" + codigoDiario + "' nao encontrado.");
    }
    if (diario.isFechado()) {
      throw new ValidacaoException("Erro: Alteracao bloqueada. O diario esta FECHADO.");
    }
  }

  private List<Matricula> buscarPauta(Diario diario) {
    List<Matricula> pauta = new java.util.ArrayList<>();
    for (Matricula matricula : matriculaRepository.buscarTodas()) {
      if (matricula.getCodigoDisciplina().equalsIgnoreCase(diario.getCodigoDisciplina())
          && matricula.getPeriodo().equalsIgnoreCase(diario.getPeriodo())
          && matricula.getStatus() == Matricula.StatusMatricula.CONFIRMADA) {
        pauta.add(matricula);
      }
    }
    return pauta;
  }

  private boolean possuiFrequencia(
      List<Frequencia> frequencias, String codigoDiario, String idAula, String aluno) {
    for (Frequencia frequencia : frequencias) {
      if (frequencia.getCodigoDiario().equalsIgnoreCase(codigoDiario)
          && frequencia.getIdAula().equalsIgnoreCase(idAula)
          && frequencia.getMatriculaAluno().equalsIgnoreCase(aluno)) {
        return true;
      }
    }
    return false;
  }

  private Nota buscarNotaDoDiario(List<Nota> notas, Diario diario, String aluno) {
    Nota legado = null;
    for (Nota nota : notas) {
      if (!nota.getMatriculaAluno().equalsIgnoreCase(aluno)
          || !nota.getCodigoDisciplina().equalsIgnoreCase(diario.getCodigoDisciplina())
          || !nota.getPeriodo().equalsIgnoreCase(diario.getPeriodo())) {
        continue;
      }
      if (nota.getCodigoDiario() != null
          && nota.getCodigoDiario().equalsIgnoreCase(diario.getCodigo())) {
        return nota;
      }
      if (nota.getCodigoDiario() == null || nota.getCodigoDiario().isBlank()) {
        legado = nota;
      }
    }
    return quantidadeDiariosDaTurma(diario) == 1 ? legado : null;
  }

  private int quantidadeDiariosDaTurma(Diario diario) {
    int quantidade = 0;
    for (Diario outro : diarioRepository.buscarTodos()) {
      if (outro.getCodigoDisciplina().equalsIgnoreCase(diario.getCodigoDisciplina())
          && outro.getPeriodo().equalsIgnoreCase(diario.getPeriodo())) {
        quantidade++;
      }
    }
    return quantidade;
  }

  private boolean possuiNotaDaEtapa(Nota nota, int etapa) {
    if (etapa == 1) return nota.getNota1() >= 0.0;
    if (etapa == 2) return nota.getNota2() >= 0.0;
    if (etapa == 3) return nota.getNota3() >= 0.0;
    return false;
  }

  private void validarPerfil(Usuario solicitante, String perfilEsperado) throws ValidacaoException {
    if (solicitante == null || !perfilEsperado.equalsIgnoreCase(solicitante.getPerfil())) {
      throw new ValidacaoException(
          "Acesso negado: esta consulta exige o perfil " + perfilEsperado + ".");
    }
  }

  private void validarTurmaExistente(String codigoDisciplina, String periodo)
      throws ValidacaoException {
    validarIdentificadorObrigatorio(codigoDisciplina, "codigo da disciplina");
    validarIdentificadorObrigatorio(periodo, "periodo");
    for (Turma turma : turmaRepository.buscarTodas()) {
      if (turma.getCodigoDisciplina().equalsIgnoreCase(codigoDisciplina)
          && turma.getPeriodo().equalsIgnoreCase(periodo)) {
        return;
      }
    }
    throw new ValidacaoException("Erro: Turma nao encontrada para a consulta de diarios.");
  }

  private void validarIdentificadorObrigatorio(String valor, String nome)
      throws ValidacaoException {
    if (valor == null || valor.isBlank()) {
      throw new ValidacaoException("Erro: O " + nome + " e obrigatorio para a consulta.");
    }
  }

  private Diario buscarDiarioObrigatorio(String codigoDiario) throws ValidacaoException {
    if (codigoDiario == null || codigoDiario.isBlank()) {
      throw new ValidacaoException("Erro: O codigo do diario e obrigatorio.");
    }
    Diario diario = diarioRepository.buscarPorCodigo(codigoDiario);
    if (diario == null) {
      throw new ValidacaoException("Erro: Diario '" + codigoDiario + "' nao encontrado.");
    }
    return diario;
  }

  private boolean alunoMatriculadoNoDiario(String matriculaAluno, Diario diario) {
    return buscarMatriculaConfirmada(matriculaAluno, diario) != null;
  }

  private Matricula buscarMatriculaConfirmada(String matriculaAluno, Diario diario) {
    for (Matricula matricula : matriculaRepository.buscarTodas()) {
      if (matricula.getMatriculaAluno().equalsIgnoreCase(matriculaAluno)
          && matricula.getCodigoDisciplina().equalsIgnoreCase(diario.getCodigoDisciplina())
          && matricula.getPeriodo().equalsIgnoreCase(diario.getPeriodo())
          && matricula.getStatus() == Matricula.StatusMatricula.CONFIRMADA) {
        return matricula;
      }
    }
    return null;
  }

  private Double obterNotaLancada(Nota nota, int etapa) {
    if (nota == null) return null;
    double valor;
    if (etapa == 1) valor = nota.getNota1();
    else if (etapa == 2) valor = nota.getNota2();
    else if (etapa == 3) valor = nota.getNota3();
    else return null;
    return valor >= 0.0 ? valor : null;
  }

  private Frequencia buscarFrequenciaDaAula(List<Frequencia> frequencias, String idAula) {
    for (Frequencia frequencia : frequencias) {
      if (frequencia.getIdAula().equalsIgnoreCase(idAula)) {
        return frequencia;
      }
    }
    return null;
  }

  private void ordenarDiarios(List<Diario> diarios) {
    diarios.sort(
        Comparator.comparing(Diario::getPeriodo, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Diario::getCodigoDisciplina, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Diario::getCodigo, String.CASE_INSENSITIVE_ORDER));
  }
}
