# Feature Specification: Calculadora financeira funcional, percentuais com gross-up e tabela progressiva

- **Identificador da spec** `011-calculadora-percentual-faixas`
- **Criada em** 2026-11-28
- **Status** Especificada e revisada
- **Base** [Descoberta financeira F0](../../docs/descoberta/financeiro/recorte.md) e [Spec 008 — Núcleo monetário](../008-nucleo-monetario/spec.md).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Executar expressões financeiras compostas sobre intermediário de alta precisão (Priority: P1)

Como desenvolvedor de sistemas financeiros ou fiscais, quero avaliar fórmulas decimais de múltiplas etapas com `MathContext` intermediário controlado e aplicar um único arredondamento monetário final na escala da moeda alvo, obtendo diretamente o tipo monetário concreto (`Real`, `Dolar`, `Euro` ou `Dinheiro`) com a política retida.

**Why this priority**: É o alicerce funcional para cálculos compostos (pró-rata, índices encadeados, bases ajustadas) onde arredondar para `2` casas a cada etapa distorce o valor final.

**Independent Test**: Executar expressões compostas em `Real`, `Dolar`, `Euro` e moedas ISO de `0` e `3` casas com `DECIMAL128` e contextos customizados (`1..90` dígitos), conferindo preservação da política retida, propagação limpa de exceções aritméticas da expressão e caracterização explícita de modos direcionais diante de dízimas arredondadas pelo `MathContext`.

**Acceptance Scenarios**:

1. **Given** uma fórmula pró-rata (`1000.00 * 17 / 30` em `DECIMAL128`) ou `(100.00 / 3) * 3` sob `ABNT_NBR_5891` ou `FINANCEIRO`, **When** avaliada em Reais, **Then** o cálculo opera em 34 dígitos significativos intermediários e realiza um único arredondamento monetário final para `Real` (`BRL 566.67` e `BRL 100.00`), enquanto modos direcionais como `TRUNCAMENTO` sobre `99.999...9` produzem deterministicamente `BRL 99.99`.
2. **Given** uma instância configurada com `CalculadoraFinanceira.de(MetodoArredondamento.TRUNCAMENTO, new MathContext(40))`, **When** utilizada para calcular montantes em Real, Dólar, Euro e Dinar kuwaitiano (`KWD/3`), **Then** cada resultado sai no subtipo monetário canônico correspondente retendo `TRUNCAMENTO`.
3. **Given** `MathContext` com precisão fora de `1..90` ou `UNNECESSARY`, função nula, retorno nulo/subclasse de `BigDecimal` ou retorno fora de `precision <= 180` / `scale` em `[-60, 180]`, **When** invocada, **Then** falha com `IllegalArgumentException`; exceções lançadas pelo corpo da expressão (como `ArithmeticException`) propagam-se sem embrulho.

---

### User Story 2 - Manipular percentuais explícitos, descontos/acréscimos conciliáveis e cálculo por dentro (Priority: P1)

Como desenvolvedor de faturamento, folha ou tributos, quero representar percentuais sem ambiguidade entre pontos percentuais (`17.5%`), inteiros (`18%`), fator unitário (`0.175`) e pontos-base (`1750 bps`), aplicando incidência direta, acréscimo, desconto e cálculo "por dentro" (*gross-up*: `valor / (1 - fator)`) com fechamento contábil garantido.

**Why this priority**: Elimina erros de escala `100x` e entrega suporte matemático exato à incidência por dentro e por fora sem acoplar leis tributárias na biblioteca.

**Independent Test**: Construir percentuais pelas quatro fábricas, testar bordas de escala (`38`, `40`, `42`), verificar igualdade, reconciliação contábil e reversibilidade incondicional do *gross-up* nos sete modos padrão.

**Acceptance Scenarios**:

1. **Given** `Percentual` criado por `de("17.5")`, `deFator("0.175")` e `dePontosBase(1750L)` (ou `de(18L)` para `18%`), **When** comparados e formatados, **Then** compartilham o mesmo fator canônico, nunca emitem notação científica em `toString()` (ex.: `de(1000L)` → `"1000%"`) e preservam fatores com precisão `<= 38` e escala `0..40`.
2. **Given** qualquer valor monetário e qualquer percentual nos sete modos padrão ou políticas customizadas válidas, **When** aplicados `aplicarSobre`, `acrescentarEm` e `descontarDe`, **Then** o subtipo estático de `Dinheiro` é preservado, `acrescentarEm(v) - v == aplicarSobre(v)` e `v - descontarDe(v) == aplicarSobre(v)`.
3. **Given** qualquer valor monetário `L` e qualquer percentual em `[0%, 100%)` nos sete modos padrão `m`, **When** calculada `B = p.calcularBasePorDentro(L, m)`, **Then** `p.descontarDe(B, m)` recompõe exatamente `L` (`B - round(B * f, m) == L`), e percentuais `< 0%` ou `>= 100%` lançam `IllegalArgumentException`.

---

### User Story 3 - Avaliar tabelas progressivas parametrizadas pelo consumidor (Priority: P2)

Como desenvolvedor que implementa faixas progressivas (comissões escalonadas, tarifas por consumo, tabelas marginais com ou sem teto), quero parametrizar faixas contíguas `(L_{k-1}, L_k]` fora da biblioteca e obter apuração auditável por faixa, parcela a deduzir matemática e alíquota efetiva nos modos `NO_TOTAL` e `POR_FAIXA`.

**Why this priority**: Entrega o motor algébrico completo para regimes progressivos mantendo as tabelas legais no sistema consumidor.

**Independent Test**: Configurar tabelas abertas e tabelas com teto (`0%` na faixa final), apurando valores na origem `0.00`, exatamente sobre fronteiras `L_k` e acima do último limite nos modos `NO_TOTAL` e `POR_FAIXA`.

**Acceptance Scenarios**:

1. **Given** uma tabela progressiva com fronteiras `L_1 < L_2`, **When** apurada sobre base exatamente igual a `L_1`, **Then** o montante permanece integralmente na 1ª faixa `[0, L_1]` e as faixas superiores aparecem na memória de cálculo com `baseNaFaixa == 0`.
2. **Given** uma apuração nos modos `NO_TOTAL` ou `POR_FAIXA`, **When** inspecionadas as faixas apuradas, **Then** a soma de `faixa.getValorApurado()` fecha 100% com `apuracao.getValorTotal()` (reconciliada por maiores restos sobre os produtos exatos no modo `NO_TOTAL`, e igual ao arredondamento individual de cada faixa em `POR_FAIXA`), enquanto `getValorExatoTotal()` preserva sempre a soma decimal exata sem arredondamento.
3. **Given** builder reutilizado após `faixaAcima`, limites não crescentes, moedas divergentes, alíquota negativa ou base negativa, **When** executado, **Then** falha deterministicamente (`IllegalStateException` no reuso de builder fechado; `IllegalArgumentException` para entradas inválidas).

---

### User Story 4 - Expressar cálculos, percentuais e tabelas em Kotlin e na vitrine de exemplos (Priority: P2)

Como desenvolvedor Kotlin ou visitante do repositório, quero extensões fluentes (`.porCento()`, `.pontosBase()`), blocos DSL (`calcularReais(arred, mc) { ... }`, `tabelaProgressivaReais { ... }`) e exemplos práticos em `brasil4j-exemplos`.

**Why this priority**: Mantém paridade idiomática Kotlin e documentação executável contínua.

**Independent Test**: Executar suítes Kotlin, `*JarIT` e exemplos em JDK 8 e JDK 21.

**Acceptance Scenarios**:

1. **Given** expressões Kotlin com `.porCento()`, `calcularReais` e `tabelaProgressivaReais` (incluindo rejeição quando `faixaAcima` é omitida no bloco DSL), **When** executadas em JDK 8 e JDK 21, **Then** delegam integralmente ao núcleo Java com resultados idênticos.

---

### Edge Cases

- Expressão em `CalculadoraFinanceira` produzindo `-0.0001` arredondada para zero: normalizada como `"BRL 0.00"` sem sinal negativo.
- `Percentual.de("0.<38 dígitos>")`: fator canônico atinge escala `40` e funciona normalmente em `aplicarSobre` sem estourar o limite de entrada monetária (`38`).
- `TabelaProgressiva` com teto (`faixaAcima(Percentual.ZERO)`): `parcelaADeduzirExata` da faixa superior pode ser negativa (documentado e matematicamente consistente: `0 * base - (-teto Contribuicao) = tetoContribuicao`).
- Base `0.00` em `ApuracaoProgressiva.getAliquotaEfetiva`: retorna `Percentual.ZERO` sem divisão por zero.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A biblioteca DEVE oferecer `@FunctionalInterface CalculoFinanceiro` e `CalculadoraFinanceira` imutável que avalie a expressão com `MathContext` configurável (`precision` em `1..90`, `roundingMode != UNNECESSARY`, padrão `DECIMAL128`) e aplique um único arredondamento monetário final na escala da moeda alvo, devolvendo `Real`, `Dolar`, `Euro` ou `Dinheiro` canônico com a política retida e propagando sem embrulho exceções lançadas pelo cálculo.
- **FR-002**: `CalculadoraFinanceira` DEVE rejeitar retornos nulos, subclasses de `BigDecimal` ou intermediários fora de `precision <= 180` e `scale` em `[-60, 180]` (verificando escala e tamanho binário antes de `precision()`), validando `precision <= 38` após o arredondamento final e normalizando zero sem sinal.
- **FR-003**: A biblioteca DEVE oferecer `Percentual` imutável construído por texto decimal canônico com ponto, `BigDecimal` ou inteiro `long` (`de`, `deFator`, `dePontosBase`, sem `float`/`double`), com invariante sobre o fator canônico (`precision <= 38`, `scale` em `0..40`, validada também em `somar`/`subtrair`/`negar`) e `toString()` plano `"<pontosPercentuais>%"` sem notação científica.
- **FR-004**: `Percentual` DEVE oferecer `<T extends Dinheiro> T aplicarSobre/acrescentarEm/descontarDe/calcularBasePorDentro` preservando o subtipo `T`, garantindo `acrescentarEm(v) - v == aplicarSobre(v)`, `v - descontarDe(v) == aplicarSobre(v)` e reversibilidade exata `descontarDe(calcularBasePorDentro(L, m), m) == L` para `[0%, 100%)` nos sete modos padrão.
- **FR-005**: A biblioteca DEVE oferecer `TabelaProgressiva<T extends Dinheiro>` com `Builder<T>` de uso único (`IllegalStateException` após `faixaAcima`), faixas contíguas `(L_{k-1}, L_k]` (`1..1_000` faixas no total), limites estritamente crescentes na mesma moeda e alíquotas `>= 0%`.
- **FR-006**: `TabelaProgressiva` DEVE apurar bases `>= 0` listando todas as faixas da tabela em `ApuracaoProgressiva<T>`, mantendo `valorExatoTotal` sempre igual à soma exata, garantindo `soma(faixa.getValorApurado()) == getValorTotal()` tanto em `NO_TOTAL` (reconciliado por Hamilton sobre os produtos exatos) quanto em `POR_FAIXA` (arredondamento individual por faixa), e calculando `getAliquotaEfetiva` sobre `valorTotal / baseCalculo`.
- **FR-007**: `brasil4j-kotlin` DEVE expor extensões genéricas e DSLs (`porCento`, `pontosBase`, `calcularReais/Dolares/Euros/EmMoeda` com `MathContext` opcional e `tabelaProgressiva*`) e `brasil4j-exemplos` DEVE demonstrar todos os casos de uso sem alterar `pom.xml` nem pacotes fora de `financas`.

### Key Entities

- **`CalculoFinanceiro` / `CalculadoraFinanceira`**: Expressão decimal intermediária e executor monetário imutável.
- **`Percentual`**: Taxa relativa exata com incidência por fora e por dentro.
- **`TabelaProgressiva` / `ModoApuracaoProgressiva` / `FaixaProgressiva` / `FaixaApurada` / `ApuracaoProgressiva`**: Motor parametrizado de faixas progressivas e memória de cálculo reconciliada.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos vetores compostos em `CalculadoraFinanceira` respeitam os limites `1..90` do `MathContext` e `180` dígitos intermediários, devolvendo subtipos canônicos e caracterizando modos simétricos e direcionais.
- **SC-002**: 100% dos vetores parametrizados de `Percentual` (incluindo bordas de escala `38..40` e todos os sete modos padrão) preservam reconciliação contábil e reversibilidade exata do *gross-up* `descontarDe(calcularBasePorDentro(L, m), m) == L`.
- **SC-003**: 100% das apurações em `TabelaProgressiva` (com e sem teto, `NO_TOTAL` e `POR_FAIXA`, base zero e fronteiras exatas `L_k`) mantêm `soma(faixa.getValorApurado()) == valorTotal`.
- **SC-004**: `clean verify` completo em JDK 8 e JDK 21 aprova 4 módulos, cobertura JaCoCo, DocLint, Dokka e bytecode 52 sem tocar em `pom.xml` nem em `identidade`/`geografia`.

## Assumptions

- Formatação localizada/SPED/CNAB e valor por extenso pertencem à Spec 012; juros, equivalência de taxas e Price/SAC pertencem à Spec 013.
- Tabelas vigentes de tributos, contribuições ou comissões pertencem exclusivamente aos consumidores.
