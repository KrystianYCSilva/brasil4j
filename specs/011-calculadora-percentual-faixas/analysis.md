# Análise de Consistência e Revisão de Plano · Spec 011 (`011-calculadora-percentual-faixas`)

- **Data:** `2026-11-28`
- **Branch / Worktree:** `011-calculadora-percentual-faixas` (`work/011-calculadora-percentual-faixas`, base `5405591`)

## 1. Resolução Integral da Revisão de Plano (Claude — 14 Achados)

| # | Gravidade | Achado | Resolução nos Artefatos |
| --- | --- | --- | --- |
| 1 | ALTA | `MathContext` intermediário arredonda dízimas; sob `TRUNCAMENTO`, `100*(1/3)*3` em `DECIMAL128` produz `BRL 99.99`. | Ajustados `FR-001`, `US1-1`, `D1` e JavaDoc para especificar "arredondamento monetário final único sobre intermediário de precisão finita", com teste de caracterização dedicado (`T004`). |
| 2 | ALTA | `Percentual.de("0.<38 casas>")` gera fator de escala `40` que estourava `validarLimitesDecimal` (`<= 38`). | Invariante definida sobre o fator canônico (`precision <= 38`, `scale` em `0..40`) em todas as fábricas e operações algébricas; multiplicação interna dedicada (`T005`, `T006`). |
| 3 | ALTA | `MathContext` sem teto de precisão permitia DoS ou estouro constante do limite de `180` dígitos. | `MathContext.precision` restrito a `1..90`; guarda barata `scale` + `bitLength <= 600` antes de `precision()` (`FR-001`, `FR-002`, `D1`, `T003`). |
| 4 | MÉDIA | 40 sobrecargas em `Percentual` inflavam `japicmp` e KDoc. | Substituídas por 8 métodos genéricos `<T extends Dinheiro> T ...` usando gancho `package-private` `criarMesmoSubtipo` em `Dinheiro` e extensão genérica em Kotlin (`D2`, `contracts/api.md §1.3`). |
| 5 | MÉDIA | Indefinição sobre `ArithmeticException` lançada dentro de `CalculoFinanceiro`. | Documentado que exceções do callback propagam-se sem embrulho (`FR-001`, `contracts/api.md`). |
| 6 | MÉDIA | Reversibilidade do *gross-up* vale incondicionalmente nos 6 modos padrão (`\|e(1-f) - d\| < 1u`). | Promovida a invariante testada nos 6 modos padrão em `FR-004`, `SC-002` e `T007`. |
| 7 | MÉDIA | `valorExatoTotal` ambíguo em `POR_FAIXA` e possível divergência entre soma das faixas e `valorTotal` em `NO_TOTAL`. | `valorExatoTotal` é sempre a soma exata; adicionado `diferencaArredondamento`; `FaixaApurada` expõe `valorArredondadoIndividual` e `valorApurado` (reconciliado por Hamilton em `NO_TOTAL` para que `soma(valorApurado) == valorTotal` sempre). |
| 8 | MÉDIA | Fronteiras de faixa, listagem de faixas zeradas, `parcelaADeduzirExata` negativa em teto e base de `aliquotaEfetiva`. | Intervalos `(L_{k-1}, L_k]`, todas as faixas listadas, nota explícita de parcela negativa em teto e `aliquotaEfetiva` sobre `valorTotal / baseCalculo` (`FR-005`, `FR-006`). |
| 9 | MÉDIA | Mutabilidade/reuso de `TabelaProgressiva.Builder`. | `Builder` de uso único (`IllegalStateException` após `faixaAcima`) e conferência de moeda em `paraMoeda` (`T008`). |
| 10 | MÉDIA | Menção normativa sem fonte em `D3`. | Removida atribuição normativa; ambos os modos descritos como convenções matemáticas (`CONVENCAO_TECNICA`). |
| 11 | MÉDIA | Isolamento com a `Spec 010` em `pom.xml` e docs compartilhados. | `pom.xml` intocado; alterações restritas ao pacote `financas` e acréscimos isolados em docs para reconciliação limpa no rebase. |
| 12–14 | BAIXA | Nomes (`CalculadoraFinanceira.de`, `getFatorAcrescimo`), `MathContext` no DSL Kotlin, `de(long)`, `toPlainString()` e granularidade de `tasks.md`. | Alinhados integralmente no contrato e divididos em tarefas atômicas `T001`–`T012`. |

## 2. Rastreabilidade Pré-Código (`FR` → `Tasks`)

- `FR-001`, `FR-002` → `T003`, `T004`
- `FR-003`, `FR-004` → `T005`, `T006`, `T007`
- `FR-005`, `FR-006` → `T008`, `T009`
- `FR-007` → `T010`, `T011`, `T012`

**Conclusão do Gate Pré-Código:** Zero inconsistências críticas, altas ou médias pendentes. Liberado para execução TDD.

## 3. Resolução Integral da Revisão Adversarial de Código (Claude — 8 Achados)

Após a primeira aprovação em JDK 8 e JDK 21, o Claude executou revisão adversarial independente sobre os 20 arquivos da Spec 011, identificando 8 pontos (`2 ALTOS`, `2 MÉDIOS`, `4 BAIXOS`). Todos foram primeiro convertidos em testes RED e corrigidos até GREEN:

| Código | Severidade | Achado | Resolução TDD (RED → GREEN) |
| --- | --- | --- | --- |
| `A1` | ALTA | `Percentual.deFator(BigDecimal)` invocava `stripTrailingZeros()` / `setScale(0)` antes de validar `scale()` e `bitLength()`, expondo DoS com `new BigDecimal("1E+999999999")` ou `BigInteger` gigante. | Guarda prévia `O(1)` (`scale` em `-37..78` e `bitLength <= 260`) inserida antes de `signum()`, `stripTrailingZeros()` e `setScale(0)` em `Percentual.deFatorInterno` (e ordem reforçada em `AritmeticaMonetaria.validarLimitesDecimal`). Coberto por `assertTimeoutPreemptively(200ms)` em `PercentualTest`. |
| `A2` | ALTA | Rateio proporcional em `NO_TOTAL` distorcia faixas exatas em modos direcionais (ex.: faixa 1 = `R$ 75,000` e faixa 2 = `R$ 0,009` sob `TRUNCAMENTO` gerava `74,99` + `0,01` em vez de `75,00` + `0,00`). | Substituído por `AritmeticaMonetaria.reconciliarFaixasMaioresRestos`: parte das cotas inteiras $\lfloor x_i \rfloor$ em unidades mínimas e distribui apenas o resíduo $\Delta = T^* - \sum \lfloor x_i \rfloor$ às maiores partes fracionárias $x_i - \lfloor x_i \rfloor$, preservando 100% intacta qualquer faixa sem fração ($r_i = 0$). Coberto nos 7 modos padrão + política customizada em `TabelaProgressivaTest`. |
| `M1` | MÉDIA | Ausência de testes de borda para `1.000` faixas, `faixaAcima(null)`, moedas de `0` e `3` casas (`JPY`, `KWD`), `Long.MIN_VALUE.negar()`, política customizada e `casasPercentuais` inválidas. | Todos adicionados em `PercentualTest` e `TabelaProgressivaTest`. |
| `M2` | MÉDIA | Documentação e suíte de reversibilidade citavam "seis modos padrão", omitindo `MEIO_PARA_BAIXO` (`HALF_DOWN`). | Atualizados `spec.md`, `research.md`, JavaDoc de `Percentual` e `PercentualTest` (`567` combinações nos 7 modos padrão). |
| `B1` | BAIXA | No DSL Kotlin, chamar `faixaAcima` duas vezes propagava `IllegalStateException` em vez da `IllegalArgumentException` contratada. | `EscopoTabelaProgressiva` valida escopo aberto e lança `IllegalArgumentException` uniforme (`Financas.kt` + `FinancasTest.kt`). |
| `B2` | BAIXA | Ausência de `equals`, `hashCode` e `toString` em `TabelaProgressiva`, `FaixaProgressiva`, `FaixaApurada` e `ApuracaoProgressiva`. | Implementados por valor e com `toString()` legível para auditoria, cobertos em `TabelaProgressivaTest`. |
| `B3` | BAIXA | Laço de ordenação e distribuição de Hamilton duplicado em `AritmeticaMonetaria`. | Extraído o auxiliar único `distribuirUnidadesMaioresRestos`, compartilhado entre `reconciliarFaixasMaioresRestos` e `alocarHamiltonEscalado`. |
| `B4` | BAIXA | `ApuracaoProgressiva.getAliquotaEfetiva` reutilizava mensagem de erro de casas decimais de moeda em vez de casas percentuais. | Mensagem dedicada validada em `TabelaProgressivaTest`. |
