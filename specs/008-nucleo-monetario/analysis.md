# Specification Analysis Report · Spec 008 (`008-nucleo-monetario`)

**Data**: 2026-11-27 · **Revisores**: Antigravity (`speckit-analyze`) + Claude (`llm-call.ps1 -Cli claude -Mode read`).

## 1. Resolução dos Achados da Revisão Independente do Plano (Claude)

Todos os 19 achados levantados pelo Claude antes da implementação foram resolvidos nos artefatos `spec.md`, `research.md`, `data-model.md`, `contracts/api.md`, `quickstart.md`, `plan.md` e `tasks.md`:

| ID | Gravidade Original | Achado | Resolução Aplicada |
| --- | --- | --- | --- |
| C-01 | CRÍTICA | `Dinheiro` como `public interface` permitia implementações externas mutáveis quebrando simetria de `equals` e evolução `japicmp`. | Convertido em `public abstract class Dinheiro` com construtor `package-private` (`FR-003`, `D2`, `contracts/api.md §1.3`). |
| C-02 | ALTA | Risco de ciclo `<clinit>` (JLS §12.4.2) entre `Moeda` (com `default`), `MoedaPadrao` e `Real.ZERO`. | Proibida regex estática (`Pattern`); validação feita por laços puros de caracteres + teste de inicialização em JVM limpa em `T013` (`D3`). |
| C-03 | ALTA | `TaxaCambio.inverter()` com `1 / cotacao` em `DECIMAL128` não era involutiva e podia perder `0,01` em `TRUNCAMENTO`/`PISO`. | `TaxaCambio` retém `cotacaoBase` + flag `invertida`, dividindo diretamente na conversão inversa; atalhos `paraDolar`/`paraReal`/`paraEuro` aceitam ambas as direções do par (`FR-009`, `D6`). |
| C-04 | ALTA | Divisão intermediária em `DECIMAL128` causava arredondamento duplo acima de ~10^30 unidades mínimas. | Divisão direta `divide(divisor, escala, roundingMode)` para modos padrão e quociente decimal exato para políticas customizadas quando terminante, com aproximação truncada e dígito de sinalização no caso restante (`FR-007`, `D1`). |
| C-05 | ALTA | Ausência de limite de tamanho/escala em `BigDecimal` permitia DoS em `setScale` e `alocar`. | Fixados limites públicos: `String <= 64` chars, `precision <= 38`, `scale` em `[-18, 38]` e até `10_000` pesos (`FR-005`, `D3`, `data-model.md`). |
| C-06..C-19 | MÉDIA / BAIXA | TOCTOU em `Moeda`, `mesmaMoeda` no contrato, propagação de `arredondamento`, validação `FLOOR`/`CEILING` em políticas customizadas, métodos covariantes de `QuantiaMonetaria`, `100` sem `1E+2`, harmonização de nomes, `"-0.004"`, `deInteiro(long)`, não-serialização, evidências desmarcadas no plano e granularidade de ciclos TDD. | Incorporados integralmente nos artefatos e desdobrados em `T001`–`T020`. |

## 2. Coverage Summary Table (`spec.md` ↔ `tasks.md`)

| Requirement Key | Has Task? | Task IDs | Notes |
| --- | --- | --- | --- |
| `FR-001` (Moeda, ISO, constantes, equivalência) | Sim | `T002`, `T003` | Inclui anti-TOCTOU e `mesmaMoeda`. |
| `FR-002` (Políticas de arredondamento e vizinhos) | Sim | `T004`, `T005` | Enum interno + validação `FLOOR`/`CEILING`. |
| `FR-003` (Hierarquia selada `Dinheiro`) | Sim | `T006`, `T007` | `abstract class` com construtor `package-private`. |
| `FR-004` (Roteamento canônico e simetria `equals`) | Sim | `T006`, `T007` | `Real`, `Dolar`, `Euro`, `QuantiaMonetaria`. |
| `FR-005` (Sem float/double, limites anti-DoS, snapshot) | Sim | `T002`–`T007` | Limites `64` chars e precisão `38`. |
| `FR-006` (`de` arredondado vs. `exato` estrito) | Sim | `T006`, `T007` | Testado em `DinheiroNucleoTest`. |
| `FR-007` (Aritmética, divisão sem duplo arredondamento) | Sim | `T008`, `T009` | Inclui quociente terminante exato, sinalização de resto na expansão infinita e *overflow* `longValueExact`. |
| `FR-008` (`ratear` e `alocar` por Hamilton exato) | Sim | `T010`, `T011` | Inclui `1..120` parcelas e pesos zero. |
| `FR-009` (`TaxaCambio` exata/involutiva e atalhos) | Sim | `T012`, `T014` | Volta exata em `TRUNCAMENTO`/`PISO`. |
| `FR-010` (`toString` determinístico sem expoente) | Sim | `T006`, `T012` | Sem dependência de `Locale`. |
| `FR-011` (Extensões e operadores Kotlin) | Sim | `T015`–`T017` | Comutatividade `Int/Long * valor` e KDoc. |
| `FR-012` (Compatibilidade `japicmp` e JAR ITs) | Sim | `T013`, `T016`, `T019` | JDK 8 e JDK 21 + baseline vigente. |

## 3. Constrained-Field Coverage (`data-model.md` ↔ `tasks.md`)

| Field (`Entity.field`) | Canonical Location | Implementing Task IDs | Status |
| --- | --- | --- | --- |
| `Moeda.codigo` | `data-model.md` | `T003` | Coberto |
| `Moeda.casasDecimais` | `data-model.md` | `T003` | Coberto |
| `Moeda.simbolo` | `data-model.md` | `T003` | Coberto |
| `MetodoArredondamento.modo` | `data-model.md` | `T005` | Coberto |
| `PoliticaResiduo.tipo` | `data-model.md` | `T005` | Coberto |
| `Dinheiro.moeda` | `data-model.md` | `T007` | Coberto |
| `Dinheiro.valor` | `data-model.md` | `T007` | Coberto |
| `Dinheiro.arredondamento` | `data-model.md` | `T007` | Coberto |
| `TaxaCambio.moedaOrigem` | `data-model.md` | `T014` | Coberto |
| `TaxaCambio.moedaDestino` | `data-model.md` | `T014` | Coberto |
| `TaxaCambio.cotacao` | `data-model.md` | `T014` | Coberto |

## 4. Metrics

- **Total Requirements**: 12 (`FR-001`–`FR-012`) + 6 (`SC-001`–`SC-006`)
- **Total Tasks**: 20 (`T001`–`T020`)
- **Coverage %**: 100% (12/12 FRs e 11/11 campos restritos citados explicitamente)
- **Open Critical / High Issues**: 0 (concluído e revalidado)

## 5. Rodada 2 · Revisão Adversarial de Código com o Claude e Fechamento TDD

Após a implementação inicial, a revisão estática completa de 26 arquivos pelo Claude identificou 13 oportunidades adicionais de blindagem. Todos os contraexemplos foram convertidos primeiro em testes RED e corrigidos até GREEN em JDK 8 e JDK 21:

| ID | Gravidade | Achado | Resolução e Prova |
| --- | --- | --- | --- |
| R2-01 | ALTA | Risco de deadlock `<clinit>` multi-thread entre `Moeda` e `MoedaPadrao`. | Criada `ValidacaoMoeda` (sem supertipos) estabelecendo DAG estrito `ValidacaoMoeda -> Moeda -> MoedaPadrao`; provado com sonda `CyclicBarrier(2)` em `FinancasJarIT`. |
| R2-02 | ALTA | `BigDecimal` não é `final` (subclasses maliciosas burlando `equals` e `scale()`). | `exigirBigDecimalPadrao` rejeita `valor.getClass() != BigDecimal.class` em todas as entradas e retornos de políticas customizadas, além de comparar `floor.equals(normalizado)` pelo lado confiável. |
| R2-03 | MÉDIA | `ratear` sem teto superior de parcelas (`OutOfMemoryError`). | Aplicado teto `1..10_000` em `ratear` (alinhado a `alocar`) e testado com `10_001` e `Integer.MAX_VALUE`. |
| R2-04 | MÉDIA | TOCTOU na lista de pesos em `alocar`. | Captura única via `pesos.toArray()` copiada e validada em array privado antes do cálculo; provado com `AbstractList` mutante. |
| R2-05 | MÉDIA | Fração substituta curta em divisão customizada não terminante. | O callback recebe uma aproximação truncada em direção a zero em `casasDestino + 34` casas, com um dígito de sinalização assinado na casa seguinte. Uma política sensível a dígitos além desse limite pode divergir da decisão sobre o valor matemático. [Contrato](contracts/api.md) |
| R2-06..R2-13 | MÉDIA / BAIXA | `Moeda.mesmaMoeda` com moeda inválida, escala intermediária em políticas compostas, `Locale.ROOT` em `fromJdkCurrency`, `MathContext.UNLIMITED` em `getFatorAproximado`, controles C1/RTL/surrogate no símbolo, DRY `mapearFatias` e datas/listas em `fontes.md`/`README.md`. | Todos implementados, documentados e cobertos na suíte verde. |

## 6. Encerramento da auditoria de divisão customizada

O teste de regressão do Agy demonstrou que `1,00 ÷ 2⁴⁰` podia produzir `BRL 0.01` quando a política esperava o quociente decimal exato e devolveria `BRL 0.00`. O commit `c0a91af` passou a entregar o quociente exato quando a divisão termina, inclusive na divisão cambial inversa. A regressão cobre os dois sinais e uma cotação que produz um quociente terminante com 126 casas.

Em 2026-11-28, a verificação independente de `c0a91af` em Corretto 8u512 e 21.0.13 registrou 521 testes por JDK, sem falhas, erros ou pulos. Os dois JARs passaram na checagem de bytecode Java 8 e na comparação binária e de fonte com os artefatos da Spec 007. [Auditoria e sondagem do limite em divisões não terminantes](../../docs/descoberta/auditoria-008-nucleo-monetario.md)
