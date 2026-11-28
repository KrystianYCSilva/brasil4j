# Plano · Governança, qualidade de desenho, CI e gitflow (expansão da Spec 010)

**Data** 2026-11-28 · **Versão** v2, que incorpora o parecer do usuário sobre a v1 · **Estado** aguardando aprovação

## Decisões já tomadas pelo usuário

1. A constituição passa a ser escrita **em inglês**, como todo arquivo lido por IA. Specs, docs e README continuam em PT-BR.
2. A estrutura de planejamento e revisão fica **no repositório brasil4j**. No `~/.agents` global, só a skill `multi-llm-cli-delegation` é atualizada, para incluir o Antigravity e os demais CLIs da máquina. As regras globais ficam agnósticas de linguagem e de arquitetura.
3. A proteção automatizada entra **agora, como expansão da 010**, cada ferramenta avaliada pelo valor que traz para uma lib.
4. Entram também a pipeline do GitHub, as configurações do repositório e o **gitflow** (`develop` para integração, PR para a `main` em releases com tag).
5. `AGENTS.md`, `CLAUDE.md` e `GEMINI.md` passam a ser **versionados** e saem do `.gitignore`. Os diretórios de ferramentas continuam ignorados.
6. **Bootstrap do gitflow:** a `develop` parte de `5405591` e a `origin/main` fica em `18bfe9c` até a release 0.3.0. A `main` local volta a acompanhar a `origin/main`, com backup antes.
7. Os rulesets valem **sem bypass**, inclusive para o administrador.

## Diagnóstico (fatos levantados)

| # | Fato | Evidência |
| --- | --- | --- |
| D1 | `AGENTS.md`, `CLAUDE.md` e `GEMINI.md` estão no `.gitignore` e só existem na worktree da `main`. Lá, o `AGENTS.md` tem uma linha em PT e o `CLAUDE.md` está desatualizado (cita `0.1.0-SNAPSHOT`, só CNPJ e CPF). Os arquivos não são compartilhados, não chegam às worktrees e não passam por revisão. | `.gitignore` (bloco "instruções locais de assistentes") e os arquivos na worktree da `main`. |
| D2 | O gate de engenharia do plano tem um único item genérico. | `.specify/templates/plan-template.md`. |
| D3 | A constituição não tem regras numeradas para engenharia. | `.specify/memory/constitution.md`. |
| D4 | O `.context` tem cerca de 2,4 KB, está em PT e não traz critérios verificáveis de API de biblioteca, Kotlin funcional, padrões, arquitetura ou qualidade de testes. | `.context/standards/*`. |
| D5 | A `main` do GitHub **não tem proteção nem rulesets**, então um push entra direto. Só existe a branch `main`. As tags são `v0.1.0` e `v0.2.0`, e só a 0.1.0 tem release publicada. | `gh api` (`/branches`, `/rulesets` e `/protection` devolveu 404). |
| D6 | A CI roda em todo push e PR, mas compara a API só com a **v0.1.0**, que está defasada. Não há fluxo de release nem template de PR. | `.github/workflows/verify.yml`. |
| D7 | A `main` local tem a 008 e a 009 (`5405591`), que não foram enviadas. A `origin/main` está em `18bfe9c`. O POM continua em `0.2.0` mesmo depois da release 0.2.0. | `git log`, `pom.xml`. |
| D8 | Não há explicit API no Kotlin, regras de arquitetura em teste, mutation testing nem `Automatic-Module-Name`. | `pom.xml` e o MANIFEST do JAR. |

## Avaliação da automação para uma lib

| Ferramenta | Problema que resolve aqui | Custo | Decisão proposta |
| --- | --- | --- | --- |
| **Kotlin explicit API (`-Xexplicit-api=strict`)** | A diretriz oficial de bibliotecas Kotlin exige visibilidade e tipo explícitos na API pública e evita expor declarações por acidente. | Só uma flag de compilação. Pode exigir `public` explícito no adaptador. | **Adotar.** |
| **`Automatic-Module-Name`** | Sem ele, o nome do módulo JPMS deriva do nome do arquivo JAR (fonte: Java SE 17 `ModuleFinder`) e muda se o arquivo for renomeado. A lib não tem `module-info`. | Duas linhas de manifest. Não há split packages entre core e kotlin. | **Adotar**, com os nomes `io.github.brasil4j.core` e `io.github.brasil4j.kotlin` (decisão permanente) e um JarIT que confira o manifest. |
| **PIT (mutation testing)** | Mede se os testes detectam falhas, o que é exatamente a "qualidade de testes" pedida. A cobertura de 98,8% não prova isso. Exige Java 8 ou superior desde a 1.4.0. | Lento por causa dos 20 mil testes parametrizados de equivalência, que precisam ser excluídos. A versão open source não cobre Kotlin. | **Adotar no núcleo Java**, num job próprio da CI em JDK 21. O limite é definido pelo baseline medido e passa a ser gate a partir daí. |
| **ArchUnit** | Protege as fronteiras: `identidade`, `geografia` e `financas` independentes; `verificacao` e `mascara` sem dependência de `identidade`; ausência de ciclos. É valioso porque a lib já tem cinco contextos e vai crescer. | Só no escopo de teste, sem dependência de runtime. Java mínimo não documentado, a conferir no JDK 8. | **Adotar com cerca de 5 regras.** É possível viver sem ele, contando só com a revisão, mas o custo é baixo e o ganho é impedir regressão de acoplamento. Se não rodar no JDK 8, fica restrito à matriz JDK 21. |
| **japicmp** | Já cobre os dois artefatos, inclusive Kotlin. | — | **Manter e corrigir a baseline** para comparar com a última tag de release (`v*`), de forma dinâmica. |
| Lint (Checkstyle, SpotBugs, detekt) | Sobreposição parcial com DocLint e revisão. | Mais tempo de build e um tuning inicial. | **Fora desta rodada.** Fica registrado como candidato. |

## Trilha A · Governança local (inglês, com orçamento de tokens)

1. **Constituição v0.6.0 em inglês, pelo `speckit-constitution`.**
   - Os princípios I a V ganham regras numeradas (I.1 …).
   - Entra o **VI · Design and test quality**, com regras curtas: Clean Code e SOLID, DRY/KISS/YAGNI com reuso, OO em Java, Kotlin funcional, padrões justificados, conformidade de arquitetura, regras de biblioteca, qualidade de testes e revisão com lentes obrigatórias.
   - Entra o **VII · Delivery**: gitflow, PR obrigatório, checks exigidos e release por tag.
   - A constituição só enumera as regras e aponta para o `.context`, que traz os detalhes.
2. **`.context` em inglês**, com cada arquivo de até cerca de 1,2 mil tokens:
   - `README.md`: índice "tarefa → arquivo";
   - `standards/design-quality.md`;
   - `standards/java-library-api.md`: compatibilidade binária (JLS 13 e Eclipse), depreciação, visibilidade, imutabilidade, interfaces e métodos `default`;
   - `standards/kotlin-library-api.md`: diretrizes oficiais e Kotlin funcional;
   - `standards/testing.md`: TDD, qualidade de teste, testes de contrato para cada interface, caracterização e equivalência, consumo pelo JAR, mutação;
   - `patterns/design-patterns.md`: os padrões em uso, quando aplicar e evitar, checklist de oportunidades;
   - `patterns/architecture.md`: as regras que o ArchUnit vai verificar;
   - `standards/review-checklist.md`: **a fonte única das lentes**, com IDs ligados à constituição;
   - `workflows/git-flow.md`: branches, PR, release, tag e hotfix;
   - `workflows/llm-review.md`: modelo de prompt para revisão cruzada com Codex e Antigravity, reaproveitando o que funcionou nesta sessão;
   - `jvm.md` e `qualidade.md` serão removidos, com o conteúdo migrado.
3. **Versionar os arquivos de entrada**, retirando-os do `.gitignore`. Os diretórios `.claude/`, `.agents/`, `.codex/` etc. seguem ignorados.
   - `AGENTS.md` (T0, em inglês, curto): propósito, stack, diretivas, comandos, ordem de leitura e **links para todos os arquivos do `.context`**.
   - `CLAUDE.md` e `GEMINI.md`: ponteiros finos (`@AGENTS.md`). O `CLAUDE.md` mantém só o que é específico do Claude.
4. **Templates do Spec Kit:**
   - `plan-template`: Design Quality Gate com IDs e a seção obrigatória "Design decisions" (padrões considerados ou rejeitados, impacto de API, extensibilidade, estratégia de testes).
   - `tasks-template`: tarefas de revisão de desenho, testes de contrato, caracterização e revisão cruzada.
   - `spec-template`: seção "Consumidores e extensibilidade da API".
5. **Template de PR** (`.github/pull_request_template.md`), com checklist que aponta para o `review-checklist`.

## Trilha B · Automação no build (TDD e verde em JDK 8 e 21)

1. `kotlin-maven-plugin` com `-Xexplicit-api=strict`, ajustando o adaptador.
2. `maven-jar-plugin` com `Automatic-Module-Name` nos dois artefatos, mais um JarIT que confere o manifest.
3. ArchUnit em `brasil4j-core` (escopo de teste) com as regras de `patterns/architecture.md`.
4. PIT (`pitest-maven` e `pitest-junit5-plugin`) num perfil `mutation`, com alvo no núcleo e exclusão dos testes de equivalência. A primeira medição define o limite.
5. **Versão `0.3.0-SNAPSHOT`** no reator: a 0.2.0 já foi lançada e a próxima release sai pelo fluxo novo.

## Trilha C · CI e configuração do GitHub como código

1. **`verify.yml`:**
   - gatilhos `pull_request` para `develop` e `main`, e `push` para `develop`, `main` e tags `v*`;
   - `concurrency` que cancela execuções antigas;
   - nomes de job estáveis, porque viram checks exigidos;
   - japicmp contra a **última tag de release**;
   - job `mutation` em JDK 21, que gera relatório e aplica o gate.
2. **`release.yml`:** em push de tag `v*`, roda o verify, constrói e cria a GitHub Release com os JARs e o trecho do CHANGELOG. A publicação no Maven Central continua fora do escopo, conforme a Spec 004.
3. **`dependabot.yml`:** GitHub Actions e Maven, semanal, com PRs para a `develop`.
4. **Rulesets versionados** em `.github/rulesets/main.json` e `develop.json`, mais `scripts/github/apply-repo-settings.ps1`, que aplica via `gh api`:
   - PR obrigatório, com zero aprovações porque o projeto tem um único mantenedor, e conversas resolvidas;
   - checks exigidos: Verify JDK 8, Verify JDK 21, compatibilidade de API e mutation;
   - branch atualizada antes do merge;
   - sem force-push e sem exclusão;
   - **sem bypass, nem para o administrador**;
   - na `main`, só entram PRs vindos de `release/*` e `hotfix/*` (convenção documentada);
   - tags `v*` protegidas.
5. **Configurações do repositório** pelo mesmo script: `deleteBranchOnMerge`, auto-merge e métodos de merge permitidos.

## Trilha D · Global mínimo

- Revisar e commitar no `~/.agents` a mudança pendente que já documenta o `agy` (`MACHINE.md` e a skill `multi-llm-cli-delegation`).
- Acrescentar o que esta sessão confirmou:
  - o `agy` headless nega comandos de shell, por isso a instrução "use only file tools";
  - revisões longas exigem `--print-timeout 0s` e execução em segundo plano;
  - o `llm-call.ps1 -Cli codex -Mode read` levou cerca de 4,7 minutos.
- Incluir uma tabela com **todos os CLIs instalados nesta máquina**, inventariados com `Get-Command`, com papel e forma de chamada.
- Conteúdo agnóstico: nada de Java nem de brasil4j.
- Não tocar nas pastas não rastreadas de `~/.agents/skills`, que parecem uma importação automática. Fica registrado para auditoria do `skill-librarian`.

## Trilha E · Validação e entrega pelo fluxo novo

1. `clean verify` em JDK 8 e 21, japicmp, PIT e bytecode. Atualizar o `analysis.md` da 010.
2. Revisão do Codex e do Antigravity **com o novo `review-checklist`**, sobre a branch inteira.
3. **Bootstrap do gitflow.** Ações externas, cada uma confirmada no momento de executar:
   1. criar e enviar a `develop` a partir de `5405591` (008 e 009); a `origin/main` continua em `18bfe9c` até a release;
   2. aplicar rulesets e configurações;
   3. publicar a 010 como `feature/010-contratos-documentos` e abrir **PR para a `develop`**, com a CI verde e os checks exigidos;
   4. fazer o merge do PR e rodar o **pr-review na `develop`** para atestar a integridade pós-merge.
4. **Release 0.3.0**, numa etapa separada e com decisão sua: `release/0.3.0` sai da `develop`, vai por PR para a `main`, recebe a tag `v0.3.0` e a GitHub Release, e volta por merge para a `develop`. Fica pendente também a GitHub Release da `v0.2.0`, cuja tag já existe.

## Riscos

- O ArchUnit pode não rodar no JDK 8. Nesse caso, perfil só no JDK 21.
- O tempo do PIT. Mitigação: excluir os testes de equivalência e usar `withHistory`.
- O explicit API pode exigir mudanças de visibilidade no Kotlin. O japicmp confere que isso não quebra nada.
- Sem bypass, até o mantenedor depende da CI para entregar. Isso é intencional.
