package br.edu.uepb.classroompb.model;

public class Professor extends Usuario {
    public Professor(String matricula, String nome, String email, String senha) {
        super(matricula, nome, email, senha, "PROFESSOR");
    }
}