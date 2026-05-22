## 2. Fluxo de Versionamento (Git Flow)

O repositório adota um modelo de versionamento descentralizado, focado na entrega granular por User Story (US) e validação rigorosa em ambiente de testes.

### Ramificações Principais
* `main`: Ambiente de Produção. É a branch padrão (Default) do repositório, garantindo a estabilidade do sistema. Ela é estritamente protegida e atualizada apenas no dia estipulado para a entrega (Release).
* `amb/QA`: Ambiente de Qualidade e Homologação. Atua como o ambiente central para testes de integração contínua. As entregas prontas são integradas nesta branch para testes antes de chegarem à produção.
* `rel/[Data]` (ex: `rel/02062026`): Branch candidata a release. Agrupa as funcionalidades aprovadas que farão parte de uma entrega oficial em uma data específica.

### Ramificações de Desenvolvimento (Por User Story)
Para isolar o escopo de trabalho e evitar conflitos, o desenvolvimento de cada tarefa adota a seguinte convenção:
* `dev/US[numero]` (ex: `dev/US08`): Ramificação de Desenvolvimento. Onde a codificação da funcionalidade ou da tarefa ocorre ativamente.
* `rel/US[numero]` (ex: `rel/US07`, `rel/US08`): Ramificação de Entrega da User Story. Recebe o código finalizado da branch de desenvolvimento para ser encapsulado como uma entrega isolada.

### Ciclo de Integração
1. **Desenvolvimento:** O fluxo inicia-se com a criação da branch `dev/US[numero]`.
2. **Fechamento da US:** Após a conclusão e testes locais, é feito um Pull Request (PR) da branch `dev` para a branch `rel/US[numero]`.
3. **Homologação:** Realiza-se o pull da branch `rel/US[numero]` diretamente para o ambiente de qualidade (`amb/QA`).
4. **Validação:** A equipe (ou o professor) testa a funcionalidade na branch `amb/QA`. Se o comportamento for o esperado e não houver regressões, a respectiva branch `rel/US[numero]` é considerada validada.
5. **Release:** As branches `rel` validadas são integradas à branch de Release agendada (ex: `rel/02062026`) e, no dia oficial de entrega, é feito o merge definitivo para a `main`.
