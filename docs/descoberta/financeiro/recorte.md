# Descoberta da Trilha Financeira (`F0`) · Moedas, Dinheiro e Motores de Cálculo

**Data** 2026-11-27 · **Base brasil4j** `5fd9dae` (`v0.2.0`, integra a Spec 006) · **Escopo de escrita** worktree isolada `008-nucleo-monetario` (disjunta da `Spec 007` em andamento pelo Codex na árvore principal) · **Estado** recorte revisado (com parecer independente do Claude incorporado nas assinaturas finais de [contracts/api.md](../../../specs/008-nucleo-monetario/contracts/api.md), que prevalece sobre nomes preliminares deste documento).

Alinha-se à [constituição v0.4.0](../../../.specify/memory/constitution.md), à [arquitetura vigente](../../arquitetura.md) e ao item de dinheiro e moeda previsto no [roteiro de expansão](../c1/roteiro-expansao.md).

## Evidência nos consumidores e motivação

O levantamento nos repositórios consumidores locais identificou ausência de um tipo monetário comum (nenhum serviço utiliza JSR 354 / Moneta ou Joda-Money) e repetição de regras ad hoc de escala, arredondamento e formatação:

| Repositório / contexto | Evidência levantada na triagem | Problema observado |
| --- | --- | --- |
| `declaracoes-mono` (leiautes fiscais SPED/RFB) | Seis serializadores repetem `setScale(2, RoundingMode.HALF_UP)` (ex.: `DirfSerializerOficial.java:91`, `EcdSerializer.java:133`, `EcfSerializer.java:1182`), enquanto `EfdContribuicoesSerializer.java:241–247` delega a `DecimalFormat` sem modo explícito (`HALF_EVEN`). Há campos monetários em `Double` (`MitApuracaoResumo.java:30`) e `String` (`R10Debito.java:51–57`). | Mistura silenciosa entre `HALF_UP` e `HALF_EVEN`, uso de ponto flutuante binário (`Double`) em valores fiscais e duplicação de formatação com vírgula/largura fixa. |
| `uem` e `uem-npd-zk-utils` | Uso recorrente de `arredondar(double...)` (~22 arquivos), utilitário parcial em `NumberUtils` e ~25 cópias divergentes de `valorPorExtenso` e `formatarDecimal`. | Perda de exatidão decimal na passagem por `double` e dezenas de implementações locais de extenso e máscara `pt-BR`. |
| `obrigacoes-service-*` e `npd-commons` | Sem tipo próprio de moeda/dinheiro; alíquotas e fatores aparecem como literais `BigDecimal.valueOf(0.2)`, `Integer` ou `String`. | Confusão entre percentual nominal (`17.5`) e fator unitário (`0.175`), além de arredondamentos intermediários prematuros em expressões compostas. |

**Fronteira constitucional (Princípio II — DDD).** O `brasil4j` fornecerá valores monetários imutáveis, políticas explícitas de arredondamento, apresentação determinística e motores matemáticos puros (rateio sem perda de centavos, percentuais com suporte a cálculo *por dentro*, avaliador genérico de faixas progressivas, juros e amortização). Tabelas legais vigentes, regras normativas de tributos ou folha (ex.: `calcularIRRF`, alíquotas de INSS/ICMS, multas punitivas específicas) e busca online de cotações (PTAX) permanecem nos sistemas consumidores.

## Fontes normativas e técnicas candidatas

As fontes abaixo orientam a descoberta e serão individualizadas em [fontes.md](../../fontes.md) conforme cada spec entrar em vigor:

| Fonte candidata | Classe prevista | Alcance no recorte |
| --- | --- | --- |
| **ABNT NBR 5891:2014** — *Regras de arredondamento na numeração decimal* | `OFICIAL` / `NORMA_TECNICA` | Define o arredondamento decimal em três casos: algarismo seguinte `< 5` mantém; `> 5` ou `5` seguido de qualquer algarismo não zero incrementa; `5` exato (seguido apenas de zeros ou fim do número) arredonda para o algarismo **par** mais próximo. Corresponde exatamente a `BigDecimal.setScale(escala, RoundingMode.HALF_EVEN)` quando a entrada é decimal exata (`String`, `BigDecimal` ou inteiro, sem passagem por `double`/`float`). Confirmar citação de seção no exemplar da norma antes de gravar em `fontes.md`. |
| **ISO 4217** (SIX Financial Information) e **Tabela de Moedas do BCB** | `OFICIAL` | Códigos alfabéticos de três letras maiúsculas (`BRL`, `USD`, `EUR`, `JPY`, `KWD`) e respectiva quantidade de casas decimais (`2` para Real/Dólar/Euro, `0` para Iene, `3` para Dinar kuwaitiano). |
| **Metodologia PTAX / BCB** e convenções cambiais | `CONVENCAO_TECNICA` | Cotações expressas com múltiplas casas decimais (tipicamente 4 a 8 casas) que não devem ser truncadas para 2 casas antes da conversão do montante. |
| **Guias Práticos SPED (RFB)** e **Padrão FEBRABAN CNAB 240/400** | `CONVENCAO_TECNICA` | Formatação numérica com vírgula decimal sem separador de milhar (SPED) e representação posicional em unidades mínimas com preenchimento de zeros à esquerda (CNAB), para o futuro incremento de apresentação. |
| **Convenções de cálculo financeiro no Brasil** (bases `30`, `252` dias úteis B3/ANBIMA, `360` comercial, `365` civil; sistemas Price e SAC) | `CONVENCAO_TECNICA` | Fórmulas algébricas de juros simples/compostos, equivalência de taxas, método dos Maiores Restos (Hamilton) para alocação proporcional e amortização com ajuste de resíduo, para a Spec 008 e os incrementos futuros de cálculo e amortização. |

## Empacotamento e alternativas avaliadas

| Alternativa | Análise | Decisão proposta |
| --- | --- | --- |
| **Pacote `io.github.brasil4j.financas` em `brasil4j-core`** (e `io.github.brasil4j.kotlin.financas` em `brasil4j-kotlin`) | Toda a trilha financeira consiste em lógica pura sobre `java.math` (~50–80 KB de bytecode estimado, zero dependências de runtime e zero tabelas embarcadas extensas). Preserva dependência única para os consumidores, evita duplicar módulos/Dokka/GPG/CI e permite que futuros tipos bancários brasileiros (ex.: linha digitável FEBRABAN, Pix estrutural) componham `Cpf`/`Cnpj` e `Real` sem dependência cruzada entre artefatos. | **Adotar.** Reservar novos módulos Maven apenas para catálogos volumosos (`brasil4j-tabelas`). |
| **Módulos separados `brasil4j-financeiro` e `brasil4j-financeiro-kotlin`** | Isola o JAR de documentos, mas fragmenta coordenadas Maven para economizar poucas dezenas de kilobytes e dificulta composição futura entre documentos fiscais/bancários e valores em Reais. | **Descartar nesta fase.** |
| **JSR 354 (Moneta) ou Joda-Money** | Introduzem dependências externas em produção (violando o Princípio IV para o núcleo) e não cobrem nativamente as demandas medidas nos consumidores brasileiros (`valorPorExtenso`, leiautes SPED/CNAB, cálculo *por dentro* e motor genérico de faixas). | **Descartar.** Implementar de forma limpa sob Apache-2.0. |

## Desenho consolidado do contrato

### 1. `Moeda`, `MetodoArredondamento` e hierarquia fechada de `Dinheiro`

- **`Moeda` (`interface`)** — Define `codigo()` (não nulo/vazio, ASCII limpo), `casasDecimais()` (intervalo válido `0..18`) e `simbolo()`. Oferece constantes imutáveis prontas `Moeda.BRL` (`2`), `Moeda.USD` (`2`), `Moeda.EUR` (`2`) e fábrica `Moeda.iso(codigo, casasDecimais)` (3 letras ASCII `A-Z`), permitindo ao consumidor implementar moedas ou unidades próprias (ex.: índices fiscais de 4 casas).
- **`MetodoArredondamento`** — Política explícita aplicável a `BigDecimal`: `ABNT_NBR_5891` (`HALF_EVEN`), `FINANCEIRO` (`HALF_UP`), `TRUNCAMENTO` (`DOWN`), `PARA_CIMA` (`UP`), `TETO` (`CEILING`) e `PISO` (`FLOOR`), além de extensão funcional para regras específicas do consumidor.
- **`Dinheiro` (`interface`) e implementações `final` (`Real`, `Dolar`, `Euro` e `QuantiaMonetaria`)**:
  - `Real`, `Dolar` e `Euro` possuem escala fixa `2` e métodos tipados (`Real.somar(Real) -> Real`, `Real.subtrair(Real) -> Real`), impedindo somar moedas distintas (`real.somar(dolar)`) **em tempo de compilação**.
  - `QuantiaMonetaria` (`final`) atende qualquer outra `Moeda`, validando igualdade de moeda nas operações binárias e capturando na construção um **snapshot imutável** dos atributos de `Moeda` (impedindo que uma implementação externa mutável corrompa `hashCode` ou escala).
  - **Simetria de `equals`/`hashCode`:** A fábrica polimórfica `Dinheiro.de(valor, moeda, arredondamento)` devolve canonicamente instâncias de `Real` para `BRL`, `Dolar` para `USD`, `Euro` para `EUR` e `QuantiaMonetaria` para as demais moedas. Nunca coexistem duas classes distintas para o mesmo código de moeda. A igualdade compara `(moeda, valor)` com escala normalizada; `-0.00` é normalizado para `0.00`.
  - **Entrada decimal exata (sem `double`/`float`):** Construtores aceitam apenas `String`, `BigDecimal` e unidades mínimas inteiras (`long` com `longValueExact()`). `de(valor, arredondamento)` normaliza a entrada aplicando e retendo a política escolhida para operações inexatas subsequentes (`dividir`, `aplicarPercentual`), enquanto `exato(valor, arredondamento)` rejeita entradas com casas decimais além da escala da moeda.

### 2. Conversão cambial e `CalculadoraFinanceira`

- **Câmbio explícito offline:** Atalhos diretos entre tipos concretos (`real.paraDolar(taxa)`, `dolar.paraReal(taxa)`) e objeto de valor `TaxaCambio` (`origem`, `destino`, `cotacao > 0`, com `inverter(MathContext)`), evitando erros de inversão entre multiplicação e divisão.
- **`CalculadoraFinanceira` + `@FunctionalInterface CalculoFinanceiro`:** Recebe o `MetodoArredondamento` e a função de cálculo executada em alta precisão intermediária (`MathContext.DECIMAL128`, 34 dígitos significativos), arredondando **uma única vez** na entrega do tipo concreto (`CalculadoraFinanceira.emReais(...) -> Real`, `emDolares(...) -> Dolar`, `emEuros(...) -> Euro` ou `emMoeda(...) -> Dinheiro`), com suporte opcional a *checkpoints* explícitos de etapa quando exigido por leiaute.

### 3. Utilitários de apresentação e motores matemáticos puros

- **`Rateio`:** Parcelamento em `n` cotas inteiras e alocação proporcional por pesos (Método dos Maiores Restos / Hamilton e opção de resíduo na primeira ou última parcela), garantindo por invariante que a soma das parcelas seja idêntica ao `Dinheiro` original.
- **`FormatadorMonetario` e `ExtensoMonetario`:** Formatação e interpretação 100% determinísticas em JDK 8 e JDK 21 (sem depender do `NBSP` `0xA0` introduzido pelo CLDR em `NumberFormat` entre versões do Java), perfis para `pt-BR` (`R$ 1.234,56`), SPED (vírgula sem milhar), posicional em unidades mínimas (CNAB) e escrita por extenso em português (`valorPorExtenso`).
- **`Percentual`:** Fábricas claras (`de("17.5")`, `deFator("0.175")`, `deBasisPoints(1375)`), incidência direta, acréscimo, desconto e **cálculo "por dentro" (*gross-up*: `valor / (1 - p)`)** sem vínculo com qualquer tributo específico.
- **`TabelaProgressiva`:** Avaliador imutável parametrizado pelo consumidor com faixas `(limite, aliquota)`, devolvendo apuração marginal faixa a faixa, parcela a deduzir derivada e alíquota efetiva.
- **`Juros` e `Amortizacao`:** Juros simples e compostos, equivalência de taxas com potências fracionárias em aritmética decimal controlada (`DECIMAL128`), fator acumulado de série de taxas e cronogramas **Price** e **SAC** reconciliados ao centavo.

## Sequência proposta de especificações (após a Spec 007)

| Ordem | Spec candidata | Escopo | Critério de promoção |
| --- | --- | --- | --- |
| 1 | **Spec 008** · Núcleo monetário, rateio e câmbio | `Moeda`, `MetodoArredondamento`, `Dinheiro` (`Real`, `Dolar`, `Euro`, `QuantiaMonetaria`), `Rateio`, `TaxaCambio` e adaptadores Kotlin (`+`, `-`, `*`, `/`, `.reais`, `.dolares`, `.euros`). | Confirmação de `ABNT NBR 5891` em `fontes.md`, cobertura JaCoCo e provas pelos JARs em JDK 8 e 21. |
| 2 | Apresentação, leiautes e extenso | `FormatadorMonetario` (`pt-BR`, SPED, CNAB/unidades mínimas, política zero/nulo), parser estrito e `ExtensoMonetario` (`valorPorExtenso`). | Determinismo byte a byte entre JDK 8 e JDK 21 e cobertura dos casos mapeados em `declaracoes-mono` e `uem`. |
| 3 | Calculadora funcional, alíquotas e faixas | `CalculadoraFinanceira`, `CalculoFinanceiro`, `Percentual` (incluindo *gross-up*) e `TabelaProgressiva` genérica + DSL Kotlin. | Ausência de perda intermediária de centavos e isolamento total de regras tributárias legais. |
| 4 | Juros, equivalência e amortização | `Juros`, equivalência de taxas (`30`, `252`, `360`, `365`), fator acumulado de série e cronogramas **Price** e **SAC**. | Precisão decimal controlada para expoentes fracionários em Java 8 e soma exata da amortização do principal. |

## Vetores sintéticos de aceite e refutação para a revisão

1. **Empate na ABNT NBR 5891 vs. Financeiro vs. Truncamento:**
   - `"10.245"` → `ABNT`: `"10.24"` (par); `FINANCEIRO`: `"10.25"`; `TRUNCAMENTO`: `"10.24"`.
   - `"10.255"` → `ABNT`: `"10.26"` (par); `FINANCEIRO`: `"10.26"`; `TRUNCAMENTO`: `"10.25"`.
   - `"10.2450001"` → `ABNT`: `"10.25"` (resíduo não nulo após o `5` desfaz o empate!).
2. **Normalização e fábrica estrita:** `Real.de("-0.000", ABNT)` → `"0.00"`; `Real.exato("10.240", ABNT)` rejeita ou aceita conforme gramática definida na spec; `Real.exato("10.245", ABNT)` rejeita obrigatoriamente.
3. **Simetria de igualdade e imutabilidade de `Moeda`:** `Dinheiro.de("10.00", Moeda.BRL, ABNT).equals(Real.de("10.00", FINANCEIRO))` deve ser `true` e ambos devem ser instâncias de `Real`. Uma `Moeda` externa mutável alterada após construir `QuantiaMonetaria` não pode alterar `moeda().codigo()`, `casasDecimais()` ou `hashCode()`.
4. **Rateio sem perda:** `Real.de("100.00", ABNT).ratearEmReais(3)` produz soma exata `"100.00"` (`["33.34", "33.33", "33.33"]` na política padrão). Alocação de `"100.00"` pelos pesos `[1, 1, 1]` e `[70, 20, 10]` preserva 100% das unidades mínimas.
5. **Determinismo multi-JDK:** Formatação `pt-BR` de `Real.de("1234.56", ABNT)` produz exatamente `"R$ 1.234,56"` com espaço ASCII `0x20` tanto no Corretto 8 quanto no Corretto 21.

## Pedido de revisão e sincronização

- **Sincronização com o Codex:** Não alterar `docs/descoberta/c1/roteiro-expansao-revisao.md`, `docs/fontes.md`, `brasil4j-core` ou `brasil4j-kotlin` até o commit da `Spec 007`. Após o merge da `007`, vincular esta trilha ao roteiro e iniciar a `Spec 008` pelo Spec Kit.
- **Revisão cruzada (Claude / Codex / Antigravity):**
  1. Verificar se a fábrica canônica `Dinheiro.de(...)` devolvendo `Real`/`Dolar`/`Euro`/`QuantiaMonetaria` cobre todos os casos de simetria de `equals`/`hashCode` e interoperabilidade Kotlin (`value class` ou extensões diretas).
  2. Conferir nos checkouts locais de `declaracoes-mono` e `uem` se algum serializador SPED exige escala monetária diferente de `2` para `BRL` (ex.: preço unitário de 4 a 10 casas vs. valor total financeiro de 2 casas), definindo se preço unitário pertence a `BigDecimal`/`CalculadoraFinanceira` e valor liquidável pertence a `Dinheiro`.
