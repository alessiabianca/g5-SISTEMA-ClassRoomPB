# 🎓 ClassRoomPB

Um Sistema de Controle Acadêmico Simplificado, desenvolvido em **Java**, utilizando arquitetura limpa (MVC) e focado na gestão de matrículas, turmas, notas e históricos escolares via Interface de Linha de Comando (CLI).

---

## 1. 📚 Sobre o Projeto

### 🛠️ Tecnologias Utilizadas
- **Linguagem:** Java 17
- **Gerenciamento e Build:** Maven
- **Testes Unitários:** JUnit 4.13.2
- **Cobertura de Código:** JaCoCo 0.8.12 (Meta de Cobertura > 85%)
- **Padronização de Código:** Spotless + Google Java Format
- **Persistência de Dados:** Arquivos planos (`.txt`, `.csv`) e serialização binária (`.dat`) locais (`data/`).

### ⚙️ Funcionalidades Principais
O sistema possui controle rigoroso de acesso com base em Perfis (RBAC - Role-Based Access Control) atendendo a diferentes papéis (Administrador, Coordenador, Professor e Aluno).
Dentre as automações de negócio, destacam-se:
- **Consistência de Pré-requisitos:** Impede matrícula sem aprovação prévia no histórico.
- **Detector de Choque de Horários:** Valida grade de professores e alunos em tempo real.
- **Fila de Espera FIFO:** Gerencia excedentes e promove matrículas automaticamente em caso de desistências.
- **Apuração Automática:** Cruza média e frequência para decidir a situação acadêmica.

---

## 2. Fluxo de Versionamento (Git Flow)

O repositório adota um modelo de versionamento descentralizado, focado na entrega granular por User Story (US) e validação rigorosa em ambiente de testes.

### Ramificações Principais
* `main`: Ambiente de Produção. É a branch padrão (Default) do repositório, garantindo a estabilidade do sistema. Ela é estritamente protegida e atualizada apenas no dia estipulado para a entrega (Release).
* `amb/QA`: Ambiente de Qualidade e Homologação. Atua como o ambiente central para testes de integração contínua. As entregas prontas são integradas nesta branch para testes antes de chegarem à produção.
* `rel/US` (ex: `rel/US07`): Branch candidata a release. Agrupa as funcionalidades aprovadas que farão parte de uma entrega oficial em uma data específica.

### Ramificações de Desenvolvimento (Por User Story)
Para isolar o escopo de trabalho e evitar conflitos, o desenvolvimento de cada tarefa adota a seguinte convenção:
* `dev/US[numero]` (ex: `dev/US08`): Ramificação de Desenvolvimento. Onde a codificação da funcionalidade ou da tarefa ocorre ativamente.
* `rel/US[numero]` (ex: `rel/US07`, `rel/US08`): Ramificação de Entrega da User Story. Recebe o código finalizado da branch de desenvolvimento para ser encapsulado como uma entrega isolada.

### Ciclo de Integração
1. **Desenvolvimento:** O fluxo inicia-se com a criação da branch `dev/US[numero]`.
2. **Fechamento da US:** Após a conclusão e testes locais, é feito um Pull Request (PR) da branch `dev` para a branch `rel/US[numero]`.
3. **Homologação:** Realiza-se o pull da branch `rel/US[numero]` diretamente para o ambiente de qualidade (`amb/QA`).
4. **Validação:** A equipe testa a funcionalidade na branch `amb/QA`. Se o comportamento for o esperado e não houver regressões, a respectiva branch `rel/US[numero]` é considerada validada.
5. **Release:** As branches `rel` validadas são integradas à branch de Release agendada (ex: `rel/02062026`) e, no dia oficial de entrega, é feito o merge definitivo para a `main`.

---

## 3. 🚀 Como Executar o Sistema

### Pré-requisitos
- JDK 17 ou superior configurado.
- Maven instalado na máquina.

### Executando a Aplicação
A interação com a aplicação se dá totalmente por linha de comando. Navegue até o diretório `ClassRoomPB/` e execute:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="br.edu.uepb.classroompb.Main"
```

### Qualidade e Testes (JaCoCo e Spotless)
Para rodar os testes e verificar a cobertura:
```bash
mvn test
mvn org.jacoco:jacoco-maven-plugin:prepare-agent test org.jacoco:jacoco-maven-plugin:report
mvn Start-Process .\target\site\jacoco\index.html
```
Para verificar e aplicar padronização de código:
```bash
mvn spotless:check
mvn spotless:apply
```

