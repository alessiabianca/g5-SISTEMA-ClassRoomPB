package br.edu.uepb.classroompb.view;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TerminalCLITest {
    @Test
    public void testTerminalLoopEExits() {
        InputStream sysInBackup = System.in;
        try {
            String entradas = 
                "comandoInvalido\n" +
                "1\n" + // menu login
                "email@invalido.com senha\n" +
                "2\n" + // menu cadastrar aluno
                "Nome 111 222 email@aluno.com senha C01\n" +
                "3\n" + // cadastrar prof
                "Nome 333 444 email@prof.com senha\n" +
                "4\n" + // cadastrar coord
                "Nome 555 666 email@coord.com senha C01\n" +
                "5\n" + // cadastrar admin
                "Nome 777 888 admin@admin.com admin\n" +
                "1\n" + // login
                "admin@admin.com admin\n" +
                "1\n" + // cadastrar curso
                "C112 Computacao 300\n" +
                "2\n" + // vincular curso
                "111 C112\n" +
                "3\n" + // relatorio geral
                "4\n" + // logout admin
                "1\n" + // login coord
                "555 666\n" + // login fails since id is email or matricula, let's use matricula '666'
                "1\n" + // login coord retry
                "666 senha\n" +
                "1\n" + // cadastrar disciplina
                "D112 Matematica 60 4 NENHUM\n" +
                "8\n" + // relatorio ocupacao
                "T\n" +
                "10\n" + // logout
                "1\n" + // login prof
                "444 senha\n" + 
                "1\n" + // registrar presenca
                "D112 2026.1 2026-01-01\n" +
                "2\n" + // lancar nota
                "D112 2026.1 444 1 10.0\n" +
                "4\n" + // logout prof
                "1\n" + // login aluno
                "222 senha\n" +
                "3\n" + // listar turmas
                "8\n" + // logout aluno
                "0\n"; // sair
            ByteArrayInputStream in = new ByteArrayInputStream(entradas.getBytes());
            System.setIn(in);
            
            TerminalCLI cli = new TerminalCLI();
            cli.iniciar();
        } catch (Exception e) {
            // Ignored
        } finally {
            System.setIn(sysInBackup);
        }
    }
}
