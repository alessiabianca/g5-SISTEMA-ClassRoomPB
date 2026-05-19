const fs = require('fs');
const path = require('path');

const projectRoot = 'ClassRoomPB';

const directories = [
    '.github/workflows',
    'data',
    'releases',
    'src/main/java/br/edu/uepb/classroompb/model',
    'src/main/java/br/edu/uepb/classroompb/repository',
    'src/main/java/br/edu/uepb/classroompb/service/exception',
    'src/main/java/br/edu/uepb/classroompb/view',
    'src/test/java/br/edu/uepb/classroompb/service'
];

const files = {
    '.gitignore': `target/
bin/
.vscode/
.idea/
*.class
*.jar
data/*.txt
data/*.csv
.DS_Store
`,

    'pom.xml': `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>br.edu.uepb</groupId>
    <artifactId>classroompb</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
`,

    'src/main/java/br/edu/uepb/classroompb/Main.java': `package br.edu.uepb.classroompb;

import br.edu.uepb.classroompb.view.TerminalCLI;

public class Main {
    public static void main(String[] args) {
        TerminalCLI cli = new TerminalCLI();
        cli.iniciar();
    }
}
`,

    'src/main/java/br/edu/uepb/classroompb/view/TerminalCLI.java': `package br.edu.uepb.classroompb.view;

import java.util.Scanner;

public class TerminalCLI {
    
    // Instancia os módulos isolados
    private final AuthCLI authCLI = new AuthCLI();
    private final AdminCLI adminCLI = new AdminCLI();
    private final CoordenadorCLI coordenadorCLI = new CoordenadorCLI();
    private final ProfessorCLI professorCLI = new ProfessorCLI();
    private final AlunoCLI alunoCLI = new AlunoCLI();

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=========================================");
        System.out.println("Bem-vindo ao ClassRoomPB");
        System.out.println("Digite 'ajuda' para ver os comandos ou 'sair'.");
        System.out.println("=========================================");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("sair")) {
                System.out.println("Encerrando o ClassRoomPB...");
                break;
            }
            if (input.isEmpty()) continue;

            processarRoteamento(input);
        }
        scanner.close();
    }

    private void processarRoteamento(String input) {
        String comandoBase = input.split(" ")[0];

        // Roteamento baseado nos comandos definidos no projeto
        switch (comandoBase) {
            case "cadastrarAluno":
            case "cadastrarProfessor":
            case "cadastrarCoordenador":
            case "cadastrarAdministrador":
            case "login":
                authCLI.processar(input);
                break;
            
            case "cadastrarCurso":
            case "configurarPeriodo":
                adminCLI.processar(input);
                break;

            case "cadastrarDisciplina":
            case "ofertarTurma":
            case "editarTurma":
            case "cancelarTurma":
            case "gerarRelatorioOcupacao":
                coordenadorCLI.processar(input);
                break;

            case "registrarFrequencia":
            case "lancarNota":
                professorCLI.processar(input);
                break;

            case "solicitarMatricula":
            case "cancelarMatricula":
            case "consultarHistorico":
                alunoCLI.processar(input);
                break;

            case "ajuda":
                System.out.println("Comandos disponíveis dependem do seu perfil de usuário.");
                break;

            default:
                System.out.println("Comando não reconhecido pelo roteador central.");
        }
    }
}
`,

    'src/main/java/br/edu/uepb/classroompb/view/AuthCLI.java': `package br.edu.uepb.classroompb.view;

public class AuthCLI {
    public void processar(String input) {
        String comando = input.split(" ")[0];
        System.out.println("[AuthCLI] Processando comando de autenticação/cadastro: " + comando);
        // Implementar lógica de chamada aos Services de Auth
    }
}
`,

    'src/main/java/br/edu/uepb/classroompb/view/AdminCLI.java': `package br.edu.uepb.classroompb.view;

public class AdminCLI {
    public void processar(String input) {
        String comando = input.split(" ")[0];
        System.out.println("[AdminCLI] Processando comando de administrador: " + comando);
        // Implementar lógica de chamada aos Services de Admin
    }
}
`,

    'src/main/java/br/edu/uepb/classroompb/view/CoordenadorCLI.java': `package br.edu.uepb.classroompb.view;

public class CoordenadorCLI {
    public void processar(String input) {
        String comando = input.split(" ")[0];
        System.out.println("[CoordenadorCLI] Processando comando de coordenador: " + comando);
        // Implementar lógica de chamada aos Services de Coordenador (Turmas, Disciplinas)
    }
}
`,

    'src/main/java/br/edu/uepb/classroompb/view/ProfessorCLI.java': `package br.edu.uepb.classroompb.view;

public class ProfessorCLI {
    public void processar(String input) {
        String comando = input.split(" ")[0];
        System.out.println("[ProfessorCLI] Processando comando de professor: " + comando);
        // Implementar lógica de chamada aos Services de Professor (Notas, Frequencia)
    }
}
`,

    'src/main/java/br/edu/uepb/classroompb/view/AlunoCLI.java': `package br.edu.uepb.classroompb.view;

public class AlunoCLI {
    public void processar(String input) {
        String comando = input.split(" ")[0];
        System.out.println("[AlunoCLI] Processando comando de aluno: " + comando);
        // Implementar lógica de chamada aos Services de Aluno (Matricula, Historico)
    }
}
`,
    
    // Marcadores para os diretórios vazios exigidos
    'releases/relatorio-release1.pdf': '',
    'releases/relatorio-processo-release1.pdf': ''
};

// Lógica de criação
console.log(`🚀 Gerando arquitetura do ${projectRoot}...\n`);

if (!fs.existsSync(projectRoot)) {
    fs.mkdirSync(projectRoot);
}

directories.forEach(dir => {
    const fullPath = path.join(projectRoot, dir);
    if (!fs.existsSync(fullPath)) {
        fs.mkdirSync(fullPath, { recursive: true });
    }
});

Object.entries(files).forEach(([filePath, content]) => {
    const fullPath = path.join(projectRoot, filePath);
    if (!fs.existsSync(fullPath)) {
        fs.writeFileSync(fullPath, content, 'utf8');
        console.log(`📄 ${filePath} criado.`);
    }
});

console.log('\n✅ Arquitetura gerada! Pronta para commit na branch main.');