// src/main/java/br/edu/uepb/classroompb/service/PeriodoService.java
package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.repository.PeriodoRepository;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeriodoService {
    private final PeriodoRepository periodoRepository;

    public PeriodoService() {
        this.periodoRepository = new PeriodoRepository();
    }

    // Construtor alternativo para injeção de dependência nos testes unitários
    public PeriodoService(PeriodoRepository repository) {
        this.periodoRepository = repository;
    }

    public void cadastrarPeriodo(String codigo) throws ValidacaoException {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ValidacaoException("O codigo do periodo nao pode ser vazio.");
        }

        try {
            List<Periodo> periodosAtuais = periodoRepository.listarTodos();
            // Validação de duplicidade lendo o arquivo em disco (RF04 / RF08)
            for (Periodo p : periodosAtuais) {
                if (p.getCodigo().equalsIgnoreCase(codigo)) {
                    throw new ValidacaoException("Periodo ja cadastrado no sistema.");
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

            for (Periodo p : periodosAtuais) {
                if (p.getCodigo().equalsIgnoreCase(codigo)) {
                    periodoEncontrado = p;
                    break;
                }
            }

            if (periodoEncontrado == null) {
                throw new ValidacaoException("Periodo nao encontrado para ativacao.");
            }

            if ("INICIADO".equals(periodoEncontrado.getStatus())) {
                throw new ValidacaoException("Este periodo ja esta ativo/iniciado.");
            }

            // Regra de Negócio: Apenas UM período pode estar INICIADO por vez.
            // Varre a lista modificando o anterior ativo para ENCERRADO
            List<Periodo> listaAtualizada = new ArrayList<>();
            for (Periodo p : periodosAtuais) {
                if ("INICIADO".equals(p.getStatus())) {
                    listaAtualizada.add(new Periodo(p.getCodigo(), "ENCERRADO"));
                } else if (p.getCodigo().equalsIgnoreCase(codigo)) {
                    listaAtualizada.add(new Periodo(codigo, "INICIADO"));
                } else {
                    listaAtualizada.add(p);
                }
            }

            // Grava o novo estado consolidado de volta no arquivo local
            periodoRepository.atualizarTodos(listaAtualizada);
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
}