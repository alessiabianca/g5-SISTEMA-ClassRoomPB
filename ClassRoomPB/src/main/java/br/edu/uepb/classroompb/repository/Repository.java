package br.edu.uepb.classroompb.repository;

import java.util.List;

/**
 * Contrato generico para repositorios de persistencia do sistema. Define a operacao minima
 * comum obrigatoria para qualquer entidade persistida. Facilita a padronizacao e futura
 * migracao dos repositorios de arquivo para um banco de dados.
 *
 * <p>Nota: A assinatura de salvar() varia entre repositorios (alguns lancam IOException,
 * outros recebem lotes), portanto nao e declarada aqui para evitar incompatibilidades.
 *
 * @param <T> Tipo da entidade gerenciada pelo repositorio
 */
public interface Repository<T> {

  /**
   * Retorna todas as entidades do tipo T presentes no repositorio.
   *
   * @return lista de todas as entidades armazenadas
   */
  List<T> buscarTodas();
}