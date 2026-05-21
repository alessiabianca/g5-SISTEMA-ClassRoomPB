// src/main/java/br/edu/uepb/classroompb/model/Usuario.java
package br.edu.uepb.classroompb.model;

public class Usuario {
    private String matricula;
    private String nome;
    private String email;
    private String perfil; // Ex: "PROFESSOR", "ALUNO"

    public Usuario(String matricula, String nome, String email, String perfil) {
        this.matricula = matricula;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
    }

    public String getMatricula() { return matricula; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getPerfil() { return perfil; }

    @Override
    public String toString() {
        return matricula + ";" + nome + ";" + email + ";" + perfil;
    }
}