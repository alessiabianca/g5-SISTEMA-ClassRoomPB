// src/main/java/br/edu/uepb/classroompb/service/PeriodoService.java
package br.edu.uepb.classroompb.service;

import br.edu.uepb.classroompb.model.Periodo;
import br.edu.uepb.classroompb.service.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.List;

public class PeriodoService {
    // Lista temporária em memória para a Task 1832 (será integrada ao Repository na Task 1833)
    private final List<Periodo> periodosMemoria = new ArrayList<>();

    public void cadastrarPeriodo(String codigo) throws ValidacaoException {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ValidacaoException("O codigo do periodo nao pode ser vazio.");
        }

        // Validação de duplicidade (RF04 / RF08)
        for (Periodo p : periodosMemoria) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                throw new ValidacaoException("Periodo ja cadastrado no sistema.");
            }
        }

        // Seguindo o padrão de status do seu grupo: todo período nasce PLANEJADO
        Periodo novoPeriodo = new Periodo(codigo, "PLANEJADO");
        periodosMemoria.add(novoPeriodo);
    }

    public void ativarPeriodo(String codigo) throws ValidacaoException {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ValidacaoException("O codigo do periodo nao pode ser vazio.");
        }

        Periodo periodoEncontrado = null;
        for (Periodo p : periodosMemoria) {
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
        // Se houver outro INICIADO, ele é implicitamente alterado para ENCERRADO.
        for (Periodo p : periodosMemoria) {
            if ("INICIADO".equals(p.getStatus())) {
                // Força o encerramento do período anterior para abrir espaço ao novo
                // Nota: Como a classe Periodo do seu grupo não tem setter para status, 
                // substituiremos o objeto na lista para respeitar a imutabilidade deles.
                int index = periodosMemoria.indexOf(p);
                periodosMemoria.set(index, new Periodo(p.getCodigo(), "ENCERRADO"));
            }
        }

        // Atualiza o status do período atual para INICIADO
        int indexAtual = periodosMemoria.indexOf(periodoEncontrado);
        periodosMemoria.set(indexAtual, new Periodo(codigo, "INICIADO"));
    }

    public List<Periodo> listarPeriodos() {
        return new ArrayList<>(periodosMemoria);
    }
}