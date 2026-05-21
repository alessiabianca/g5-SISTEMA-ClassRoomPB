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

    public PeriodoService(PeriodoRepository repository) {
        this.periodoRepository = repository;
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

            periodoRepository.atualizarTodos(listaAtualizada);
        } catch (IOException e) {
            throw new ValidacaoException("Erro ao atualizar o armazenamento local: " + e.getMessage());
        }
    }

    /**
     * Verifica se um determinado periodo existe e esta aberto para matriculas.
     * Utilizado para validar as regras de negocio de matriculas na Release 2.
     */
    public boolean isPeriodoAberto(String codigo) throws ValidacaoException {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ValidacaoException("O codigo do periodo nao pode ser vazio.");
        }
        
        Periodo periodo = periodoRepository.buscarPorCodigo(codigo);
        if (periodo == null) {
            throw new ValidacaoException("Periodo nao encontrado.");
        }
        
        return periodo.isAbertoParaMatriculas();
    }

    public List<Periodo> listarPeriodos() {
        try {
            return periodoRepository.listarTodos();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}