# Análise de Consistência e Revisão Independente · Spec 009 (`brasil4j-exemplos`)

- **Data:** `2026-11-27`
- **Branch / Worktree:** `009-exemplos-utilizacao` (`work/009-exemplos-utilizacao`)
- **Base:** `008-nucleo-monetario` (`517280f`)

## 1. Alinhamento entre `spec.md`, `plan.md`, `tasks.md` e Código

| Requisito / Critério | Evidência na Implementação | Status |
| --- | --- | --- |
| **FR-001 / SC-005** · Zero artefatos em `src/main` | `brasil4j-exemplos` possui apenas `src/test/java` e `src/test/kotlin` | Atendido |
| **FR-002 / SC-002** · Execução contínua em `clean verify` (JDK 8 e 21) | Perfil `exemplos` (`activeByDefault=true`) + verificação explícita de `TEST-*.xml` em `.github/workflows/verify.yml` | Atendido |
| **FR-003 / SC-003** · Isolamento estrito de publicação (`-Pcentral`) | Desativação automática sob `-Pcentral` + perfil `central` com `maven-enforcer-plugin` (`<alwaysFail>`) em `brasil4j-exemplos/pom.xml` + flags `*.skip=true` | Atendido |
| **FR-004 / SC-001** · Cobertura Java 8 dos 3 domínios | `IdentidadeExemplosTest`, `GeografiaExemplosTest` e `FinancasExemplosTest` | Atendido |
| **FR-005 / SC-001** · Cobertura Kotlin idiomática dos 3 domínios | `IdentidadeGeografiaKotlinExemplosTest` e `FinancasKotlinExemplosTest` | Atendido |
| **FR-006 / SC-004** · Vetores sintéticos ou públicos | CNPJ `11.222.333/0001-81` e `12ABC34501DE35` (RFB), CPF `28001238938` (suíte existente), NIS `00000000280` (descoberta Spec 006) | Atendido |
| **FR-007** · Documentação navegável | `brasil4j-exemplos/README.md` com índice clicável e comandos multiplataforma + seção no `README.md` raiz | Atendido |

## 2. Revisão Adversarial Independente (Claude)

- **Parecer inicial:** *Aprovado com ressalvas* (0 críticos/altos, 4 médios, 9 baixos).
- **Resolução integral dos 13 apontamentos:**
  1. **[MÉDIA #1] Regressão silenciosa de `activeByDefault`:** Adicionado passo `Assert examples module ran` em `.github/workflows/verify.yml` conferindo `brasil4j-exemplos/target/surefire-reports/TEST-*.xml` em JDK 8 e JDK 21, além de comentário preventivo no `pom.xml` raiz.
  2. **[MÉDIA #2] Trava rígida contra `-Pcentral,exemplos`:** Adicionado `<profile><id>central</id>` em `brasil4j-exemplos/pom.xml` executando `maven-enforcer-plugin` com `<alwaysFail>` na fase `validate`.
  3. **[MÉDIA #3] Demonstração real de `TaxaCambio.inverter()`:** `FinancasExemplosTest.exemploConversaoCambialOffline` agora demonstra tanto `Real.paraEuro(ptaxEur)` (direto) quanto `((Dinheiro) Real.de("610.00")).converter(ptaxEur.inverter())` (contrato polimórfico).
  4. **[MÉDIA #4] Cobertura idiomática de `Uf` em Kotlin:** `IdentidadeGeografiaKotlinExemplosTest.exemploGeografiaEmKotlin` expandido com `Uf.fromName("  parana  ").orElse(null)`, `ddds` e `Uf.fromIbgeCode("53").orElse(null)`.
  5. **[BAIXA #5] Exemplos de falha esperada em Kotlin:** Adicionados blocos `assertThrows<IllegalArgumentException>` em `IdentidadeGeografiaKotlinExemplosTest` (`.toCpf(NUMERIC)`) e `FinancasKotlinExemplosTest` (`Real.exato` e soma polimórfica entre moedas distintas).
  6. **[BAIXA #6] Clareza didática nas políticas de NIS do consumidor:** Simplificado para `nis -> nis.getCanonicalValue().chars().distinct().count() > 1` (Java) e `{ it.canonicalValue.toSet().size > 1 }` (Kotlin).
  7. **[BAIXA #7] Asserção direta de `Cpf.getBase()`:** Substituído `substring(0, 3)` por `assertEquals("280012389", cpfValidoMatematicamente.getBase())`.
  8. **[BAIXA #8] Rotulagem da política customizada de arredondamento:** Comentada explicitamente como política ilustrativa (não normativa).
  9. **[BAIXA #9] Eliminação de duplicação nos POMs:** Configuração base do `kotlin-maven-plugin` (`jvmTarget=1.8`, `-Xjdk-release=8`) centralizada em `<pluginManagement>` no `pom.xml` raiz; removidos o bloco redundante de `maven-compiler-plugin` e a dependência explícita duplicada de `kotlin-stdlib` em `brasil4j-exemplos/pom.xml`.
  10. **[BAIXA #10] Documentação das propriedades `*.skip`:** Comentadas em `brasil4j-exemplos/pom.xml` como defesa em profundidade.
  11. **[BAIXA #11] Registro arquitetural sobre o POM pai publicado:** Documentado em `research.md` (`D1`, item 3).
  12. **[BAIXA #12] Comandos multiplataforma no guia do módulo:** `brasil4j-exemplos/README.md` atualizado com `-pl brasil4j-exemplos -am` para `sh ./mvnw` e `.\mvnw.cmd`.
  13. **[BAIXA #13] Rastreabilidade em `tasks.md`:** Tarefas `T001`–`T005` marcadas como concluídas com evidência datada em ISO 8601 (`2026-11-27`).

## 3. Segunda Rodada · Expansão para 7 Suítes e 28 Cenários de Uso (`2026-11-27`)

- **Escopo expandido:** Adicionados cenários corporativos reais por domínio e suítes integradas ponta a ponta (`CenariosIntegradosExemplosTest` em Java 8 e `CenariosIntegradosKotlinExemplosTest` em Kotlin), cobrindo 100% das classes, métodos, políticas e extensões públicas de `identidade`, `geografia` e `financas`.
- **Revisão independente da expansão (Claude):** parecer *Aprovado com ressalvas* (1 médio, 7 baixos), com todos os 8 refinamentos aplicados:
  1. Explicitação do caráter estritamente estrutural de `Nis` (`"00000000281"` aceito sem política) e demonstração de `Predicate<Nis>` com módulo 11 do PIS pelo consumidor.
  2. Asserção literal `assertEquals("BRL 0.00", Real.ZERO.negar().toString())`.
  3. Precisão na tabela do `README.md` sobre `*` comutativo (`Int`/`Long`) vs. `* BigDecimal` à direita.
  4. Verificação de ambas as UFs não nulas em `PedidoInterestadual.interestadual`.
  5. Conferência adicional de não vazamento do formato mascarado do CPF no `toString()` de `data class` Kotlin.
  6. Uso de `BigDecimal` com nota explícita de alíquota ilustrativa em `GeografiaExemplosTest`.
  7. Padronização ortográfica em PT-BR (`Agregacao`, `centavos`).
  8. Demonstração explícita de `compararMesmaMoeda`, `getMoeda`, `getArredondamento`, `Dinheiro.exato`, `Dinheiro.deUnidadesMinimas`, `TaxaCambio.converter`, involução `inverter().inverter()`, operadores polimórficos em `Dinheiro`, `alocarPorPesos` em todas as moedas e `Nis(null)`.

