package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.repository.DiarioRepository;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeriodoService {
  private final PeriodoRepository periodoRepository;
  private HistoricoService historicoService;
  private DiarioRepository diarioRepository;

  public PeriodoService() {
    this.periodoRepository = new PeriodoRepository();
    this.diarioRepository = new DiarioRepository();
  }

  public PeriodoService(PeriodoRepository repository) {
    this.periodoRepository = repository;
    this.diarioRepository = new DiarioRepository();
  }

  public PeriodoService(PeriodoRepository repository, HistoricoService historicoService) {
    this(repository, historicoService, new DiarioRepository());
  }

  public PeriodoService(
      PeriodoRepository repository,
      HistoricoService historicoService,
      DiarioRepository diarioRepository) {
    this.periodoRepository = repository;
    this.historicoService = historicoService;
    this.diarioRepository = diarioRepository;
  }

  public void cadastrarPeriodo(String codigo) throws ValidacaoException {
    if (codigo == null || codigo.trim().isEmpty()) {
      throw new ValidacaoException("O codigo do periodo nao pode ser vazio.");
    }

    try {
      List<Periodo> periodosAtuais = periodoRepository.listarTodos();
      for (Periodo p : periodosAtuais) {

        if (p.getCodigo().equalsIgnoreCase(codigo)) {
          throw new ValidacaoException("Periodo ja cadastrado no sistema.");
        }

        if (!p.getCodigo().equalsIgnoreCase(codigo)
            && ("INICIADO".equals(p.getStatus()) || "PLANEJADO".equals(p.getStatus()))) {
          throw new ValidacaoException(
              "Já possui um periodo (" + p.getCodigo() + ") cadastrado no sistema.");
        }
      }

      Periodo novoPeriodo = new Periodo(codigo, "PLANEJADO");
      periodoRepository.salvar(novoPeriodo);
    } catch (IOException e) {
      throw new ValidacaoException("Erro ao acessar o armazenamento local: " + e.getMessage());
    }
  }

  public void activarPeriodo(String codigo) throws ValidacaoException {
    if (codigo == null || codigo.trim().isEmpty()) {
      throw new ValidacaoException("O codigo do periodo nao pode ser vazio.");
    }

    try {
      List<Periodo> periodosAtuais = periodoRepository.listarTodos();
      Periodo periodoEncontrado = null;
      boolean existePeriodoAtivo = false;

      for (Periodo p : periodosAtuais) {
        if (p.getCodigo().equalsIgnoreCase(codigo)) {
          periodoEncontrado = p;
        }
        if ("INICIADO".equalsIgnoreCase(p.getStatus())) {
          existePeriodoAtivo = true;
        }
      }

      if (periodoEncontrado == null) {
        throw new ValidacaoException("Periodo nao encontrado para ativacao.");
      }

      if ("INICIADO".equalsIgnoreCase(periodoEncontrado.getStatus())) {
        throw new ValidacaoException("Este periodo ja esta ativo/iniciado.");
      }

      if (existePeriodoAtivo) {
        throw new ValidacaoException(
            "Nao eh possivel ativar este periodo. Ja existe um periodo letivo ativo no sistema.");
      }

      List<Periodo> listaAtualizada = new ArrayList<>();
      for (Periodo p : periodosAtuais) {
        if (p.getCodigo().equalsIgnoreCase(codigo)) {
          listaAtualizada.add(new Periodo(codigo, "INICIADO"));
        } else {
          listaAtualizada.add(p);
        }
      }

      periodoRepository.atualizarTodos(listaAtualizada);
    } catch (IOException e) {
      throw new ValidacaoException("Erro ao atualizar o armazenamento local: " + e.getMessage());
    }
  }

  public void encerrarPeriodo(String codigo) throws ValidacaoException {
    if (codigo == null || codigo.trim().isEmpty()) {
      throw new ValidacaoException("O codigo do periodo nao pode ser vazio.");
    }

    try {
      List<Periodo> periodosAtuais = periodoRepository.listarTodos();
      Periodo periodoEncontrado = null;

      for (Periodo p : periodosAtuais) {
        if (p.getCodigo().equalsIgnoreCase(codigo)) {
          periodoEncontrado = p;
          break;
        }
      }

      if (periodoEncontrado == null) {
        throw new ValidacaoException("Periodo nao encontrado para encerramento.");
      }

      if ("ENCERRADO".equalsIgnoreCase(periodoEncontrado.getStatus())) {
        throw new ValidacaoException("Este periodo ja se encontra encerrado.");
      }

      if (!"INICIADO".equalsIgnoreCase(periodoEncontrado.getStatus())) {
        throw new ValidacaoException("Apenas periodos iniciados/ativos podem ser encerrados.");
      }

      validarDiariosFechados(codigo);

      List<Periodo> listaAtualizada = new ArrayList<>();
      for (Periodo p : periodosAtuais) {
        if (p.getCodigo().equalsIgnoreCase(codigo)) {
          listaAtualizada.add(new Periodo(codigo, "ENCERRADO"));
        } else {
          listaAtualizada.add(p);
        }
      }

      periodoRepository.atualizarTodos(listaAtualizada);

      if (historicoService != null) {
        historicoService.gerarHistoricoDoPeriodo(codigo);
      }
    } catch (IOException e) {
      throw new ValidacaoException("Erro ao atualizar o armazenamento local: " + e.getMessage());
    }
  }

  public List<Periodo> listarPeriodos() {
    try {
      return periodoRepository.listarTodos();
    } catch (IOException e) {
      return new ArrayList<>();
    }
  }

  private void validarDiariosFechados(String codigoPeriodo) throws ValidacaoException {
    if (diarioRepository == null) {
      return;
    }
    List<String> abertos = new ArrayList<>();
    for (br.edu.uepb.classroompb.model.Diario diario : diarioRepository.buscarTodos()) {
      if (diario.getPeriodo().equalsIgnoreCase(codigoPeriodo) && !diario.isFechado()) {
        abertos.add(diario.getCodigo());
      }
    }
    if (!abertos.isEmpty()) {
      throw new ValidacaoException(
          "Nao e possivel encerrar o periodo: diarios ainda abertos: "
              + String.join(", ", abertos)
              + ".");
    }
  }
}
