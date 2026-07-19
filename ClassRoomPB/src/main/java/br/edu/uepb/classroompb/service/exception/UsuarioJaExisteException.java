package br.edu.uepb.classroompb.service.exception;

public class UsuarioJaExisteException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public UsuarioJaExisteException(String message) {
    super(message);
  }
}
