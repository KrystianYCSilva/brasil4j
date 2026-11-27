# Data Model · Spec 008 — Núcleo monetário, rateio e câmbio

Pacote Java canônico: `io.github.brasil4j.financas` (`brasil4j-core`).
Pacote Kotlin de extensões: `io.github.brasil4j.kotlin.financas` (`brasil4j-kotlin`).

## Entidades e Campos com Restrições Canônicas

| Entidade (`Entity`) | Campo (`Entity.field`) | Tipo | Restrições e Invariantes Canônicas |
| --- | --- | --- | --- |
| `Moeda` | `Moeda.codigo` | `String` | Obrigatório (não nulo, não vazio); comprimento `1..16` caracteres ASCII visíveis (`0x21..0x7E`, sem espaços ou controle), validado por laço de caracteres sem regex estática. Em `Moeda.iso(...)`, restrito a `3` letras maiúsculas ASCII (`[A-Z]{3}`). |
| `Moeda` | `Moeda.casasDecimais` | `int` | Obrigatório; inteiro no intervalo fechado `0..18`. Constantes `BRL`, `USD` e `EUR` têm valor fixo `2`. |
| `Moeda` | `Moeda.simbolo` | `String` | Obrigatório (não nulo, não vazio); comprimento `1..16` caracteres sem caracteres de controle (`< 0x20` ou `0x7F`). Quando `Moeda` externa é capturada, `getCodigo()`, `getCasasDecimais()` e `getSimbolo()` são lidos **uma única vez** em variáveis locais antes da validação (anti-TOCTOU). |
| `MetodoArredondamento` | `MetodoArredondamento.modo` | `@FunctionalInterface` | Constantes pré-definidas obrigatórias via enum interno `ModoArredondamentoPadrao`: `ABNT_NBR_5891` (`HALF_EVEN`), `FINANCEIRO` (`HALF_UP`), `TRUNCAMENTO` (`DOWN`), `PARA_CIMA` (`UP`), `TETO` (`CEILING`), `PISO` (`FLOOR`), mais políticas customizadas do consumidor. Nunca nulo; toda execução deve retornar `BigDecimal` não nulo com `scale() == casasDecimais` igual a `FLOOR` ou `CEILING` do valor de entrada nessa escala. |
| `PoliticaResiduo` | `PoliticaResiduo.tipo` | `enum` | Valores obrigatórios: `PRIMEIRAS_PARCELAS`, `ULTIMAS_PARCELAS`. Nunca nulo. |
| `Dinheiro` (`abstract class`) / `Real`, `Dolar`, `Euro`, `QuantiaMonetaria` (`final`) | `Dinheiro.moeda` | `Moeda` | Obrigatório (não nulo); armazenado na classe base como `MoedaPadrao` imutável. Para `("BRL", 2)` é sempre canonicamente `Moeda.BRL` (`"R$"`); para `("USD", 2)` é `Moeda.USD` (`"US$"`); para `("EUR", 2)` é `Moeda.EUR` (`"€"`). `QuantiaMonetaria` rejeita esses três pares reservados. |
| `Dinheiro` (`Real`, `Dolar`, `Euro`, `QuantiaMonetaria`) | `Dinheiro.valor` | `BigDecimal` | Obrigatório (não nulo); `scale() == Dinheiro.moeda.casasDecimais` e `precision() <= 38`. Entradas textuais aceitam até `64` caracteres na gramática `[+-]?[0-9]+(\.[0-9]+)?`; entradas `BigDecimal` exigem `precision() <= 38` e `scale()` em `[-18, 38]`. No modo `exato`, rejeita entradas cuja escala fornecida exceda `casasDecimais` (ex.: `"10.500"` e `"10.555"` rejeitados para escala `2`). Qualquer zero resultante tem `BigDecimal.ZERO.setScale(casasDecimais)`. |
| `Dinheiro` (`Real`, `Dolar`, `Euro`, `QuantiaMonetaria`) | `Dinheiro.arredondamento` | `MetodoArredondamento` | Obrigatório (não nulo); assume `ABNT_NBR_5891` quando omitido nas fábricas de conveniência. Preservado nas operações que não informam nova política; substituído e retido nas sobrecargas que recebem `MetodoArredondamento` explícito. Excluído de `equals`/`hashCode`. |
| `TaxaCambio` | `TaxaCambio.moedaOrigem` | `Moeda` | Obrigatório (não nulo); `MoedaPadrao` imutável; deve diferir de `TaxaCambio.moedaDestino` (`!Moeda.mesmaMoeda(origem, destino)`). |
| `TaxaCambio` | `TaxaCambio.moedaDestino` | `Moeda` | Obrigatório (não nulo); `MoedaPadrao` imutável; deve diferir de `TaxaCambio.moedaOrigem`. |
| `TaxaCambio` | `TaxaCambio.cotacao` | `BigDecimal` | Obrigatório (não nulo); estritamente positivo (`> 0`), com entrada limitada a `<= 64` caracteres textuais, `precision() <= 38` e `scale()` em `[-18, 38]`, normalizado internamente sem escala negativa (`scale >= 0`, nunca notação científica `"1E+2"`). Taxas invertidas retêm a `cotacaoBase` original e o indicador `invertida = true` para inversão involutiva exata. |

## Regras de Identidade, Igualdade e Serialização

1. **Hierarquia selada e não serializável**: `Dinheiro` é `public abstract class` com construtor `package-private`; `Real`, `Dolar`, `Euro`, `QuantiaMonetaria` e `TaxaCambio` são `public final class`. Nenhum tipo implementa `java.io.Serializable`.
2. **Igualdade de `Moeda`**: `Moeda.mesmaMoeda(a, b)` compara `(codigo, casasDecimais)` após validação não nula. `MoedaPadrao.equals` aceita apenas `MoedaPadrao` comparando `(codigo, casasDecimais)`, preservando simetria total.
3. **Igualdade de `Dinheiro`**: `final` em `Dinheiro`, comparando `(moeda.codigo, moeda.casasDecimais, valor)`. `toString()` é `final`: `<codigo> <valor.toPlainString()>`.
4. **Igualdade e representação de `TaxaCambio`**: `TaxaCambio` criada diretamente (`1 A = c B`, `invertida = false`) e uma taxa invertida (`c B = 1 A`, `invertida = true`) comparam em `equals`/`hashCode` a forma canônica `(moedaBase, moedaCotada, cotacaoBaseNormalizada, invertida)` — com caso especial `cotacaoBase == 1`: quando `cotacaoBase` é `1`, inverter de `A -> B` para `B -> A` equivale canonicamente a `1 B = 1 A` direto! `toString()` produz `"1 <ORIGEM> = <COTACAO> <DESTINO>"` quando direta e `"<COTACAO_BASE> <ORIGEM> = 1 <DESTINO>"` quando invertida.
