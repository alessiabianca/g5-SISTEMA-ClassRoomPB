package br.edu.uepb.classroompb.repository;

import br.edu.uepb.classroompb.model.Usuario;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioRepository {
  private static final String FILE_NAME = "data/usuarios.json";
  private Map<String, Usuario> dados;
  private final Gson gson;

  public UsuarioRepository() {
    this.gson =
        new GsonBuilder()
            .registerTypeAdapter(Usuario.class, new UsuarioAdapter())
            .setPrettyPrinting()
            .create();

    File file = new File(FILE_NAME);
    if (file.exists()) {
      try (Reader reader = new FileReader(file)) {
        Type type = new TypeToken<Map<String, Usuario>>() {}.getType();
        this.dados = gson.fromJson(reader, type);
        if (this.dados == null) {
          this.dados = new HashMap<>();
        }
      } catch (Exception e) {
        System.err.println(
            "Aviso: Falha ao carregar dados do disco. Inicializando base vazia. Erro: "
                + e.getMessage());
        this.dados = new HashMap<>();
      }
    } else {
      this.dados = new HashMap<>();
    }
  }

  public boolean existe(String identificador) {
    if (identificador == null) return false;

    if (dados.containsKey(identificador)) {
      return true;
    }

    for (Usuario u : dados.values()) {
      if (identificador.equalsIgnoreCase(u.getMatricula())
          || identificador.equalsIgnoreCase(u.getEmail())) {
        return true;
      }
    }
    return false;
  }

  public void salvar(Usuario usuario) {
    if (usuario == null) return;

    String chave =
        (usuario.getMatricula() != null && !usuario.getMatricula().isEmpty())
            ? usuario.getMatricula()
            : usuario.getEmail();

    if (chave != null && !chave.isEmpty()) {
      dados.put(chave, usuario);
      sincronizarComDisco();
    }
  }

  public Usuario buscarPorId(String identificador) {
    if (identificador == null) return null;

    if (dados.containsKey(identificador)) {
      return dados.get(identificador);
    }

    for (Usuario u : dados.values()) {
      if (identificador.equalsIgnoreCase(u.getMatricula())
          || identificador.equalsIgnoreCase(u.getEmail())) {
        return u;
      }
    }
    return null;
  }

  public Usuario buscarPorMatricula(String matricula) {
    if (matricula == null || matricula.isBlank()) {
      return null;
    }

    for (Usuario usuario : dados.values()) {
      if (matricula.equalsIgnoreCase(usuario.getMatricula())) {
        return usuario;
      }
    }
    return null;
  }

  public List<Usuario> buscarTodos() {
    return new ArrayList<>(dados.values());
  }

  public boolean atualizarCurso(String matricula, String codigoCurso) {
    Usuario usuario = buscarPorMatricula(matricula);
    if (usuario == null) {
      return false;
    }
    usuario.setCodigoCurso(codigoCurso);
    sincronizarComDisco();
    return true;
  }

  private void sincronizarComDisco() {
    try (Writer writer = new FileWriter(FILE_NAME)) {
      gson.toJson(dados, writer);
    } catch (IOException e) {
      System.err.println(
          "Erro crítico: Falha de escrita ao salvar os dados dos usuários no disco em formato JSON. Erro: "
              + e.getMessage());
    }
  }

  private static class UsuarioAdapter
      implements JsonDeserializer<Usuario>, JsonSerializer<Usuario> {
    @Override
    public Usuario deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      JsonObject obj = json.getAsJsonObject();
      String perfil = obj.get("perfil").getAsString();
      String matricula = obj.has("matricula") ? obj.get("matricula").getAsString() : null;
      String nome = obj.has("nome") ? obj.get("nome").getAsString() : null;
      String email = obj.has("email") ? obj.get("email").getAsString() : null;
      String senha = obj.has("senha") ? obj.get("senha").getAsString() : null;

      switch (perfil.toUpperCase()) {
        case "ALUNO":
          String cursoAluno =
              obj.has("codigoCurso") && !obj.get("codigoCurso").isJsonNull()
                  ? obj.get("codigoCurso").getAsString()
                  : null;
          return new br.edu.uepb.classroompb.model.Aluno(matricula, nome, email, senha, cursoAluno);
        case "PROFESSOR":
          return new br.edu.uepb.classroompb.model.Professor(matricula, nome, email, senha);
        case "COORDENADOR":
          String cursoCoord =
              obj.has("codigoCurso") && !obj.get("codigoCurso").isJsonNull()
                  ? obj.get("codigoCurso").getAsString()
                  : null;
          return new br.edu.uepb.classroompb.model.Coordenador(
              matricula, nome, email, senha, cursoCoord);
        case "ADMINISTRADOR":
          return new br.edu.uepb.classroompb.model.Administrador(matricula, nome, email, senha);
        default:
          throw new JsonParseException("Perfil de usuário desconhecido: " + perfil);
      }
    }

    @Override
    public JsonElement serialize(Usuario src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject obj = new JsonObject();
      obj.addProperty("matricula", src.getMatricula());
      obj.addProperty("nome", src.getNome());
      obj.addProperty("email", src.getEmail());
      obj.addProperty("senha", src.getSenha());
      obj.addProperty("perfil", src.getPerfil());
      if (src.getCodigoCurso() != null) {
        obj.addProperty("codigoCurso", src.getCodigoCurso());
      }
      return obj;
    }
  }
}
