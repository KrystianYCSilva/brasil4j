# Research · Spec 008 — Núcleo monetário, rateio e câmbio

## D1 · Arredondamento ABNT NBR 5891, divisão exata sem arredondamento duplo e vizinhos válidos

- **Decision**:
  1. Implementar as seis políticas embutidas (`ABNT_NBR_5891`, `FINANCEIRO`, `TRUNCAMENTO`, `PARA_CIMA`, `TETO`, `PISO`) por meio de um `enum` `package-private` (`ModoArredondamentoPadrao implements MetodoArredondamento`) que encapsula `RoundingMode` (`HALF_EVEN`, `HALF_UP`, `DOWN`, `UP`, `CEILING`, `FLOOR`), garantindo identidade estável e `toString()` legível. Nota de proveniência: registrar a equivalência matemática com `HALF_EVEN` (documentada publicamente em manuais técnicos fiscais/metrológicos brasileiros que citam a ABNT NBR 5891) como `CONVENCAO_TECNICA` até conferência direta de página/seção em exemplar licenciado da norma.
  2. Em `dividir(long)` e na divisão cambial inversa: quando a política for `ModoArredondamentoPadrao`, executar `numerador.divide(denominador, casasDecimais, modo.roundingMode())` diretamente (divisão decimal exata em passo único, sem `MathContext.DECIMAL128` intermediário e sem arredondamento duplo em qualquer magnitude!). Quando a política for customizada, usar divisão exata para quociente decimal terminante. Para expansão infinita, truncar em direção a zero na escala de destino mais 34 casas e acrescentar na próxima casa um dígito 1 com o sinal do quociente se houver resto. Invocar a política uma vez e exigir a escala de destino e um dos dois vizinhos imediatos do operando.
  3. Em `multiplicar(BigDecimal)`, usar multiplicação exata `valor.multiply(fator)` (sem `MathContext`) seguida de arredondamento único. Proibir `double` e `float` em toda a API pública.
- **Rationale**: Elimina o arredondamento duplo identificado na revisão arquitetural em valores de grande magnitude e blinda a invariante de `Dinheiro` contra funções customizadas inconsistentes.
- **Alternatives considered**: Dividir em `DECIMAL128` antes de `setScale` — rejeitado por arredondar duas vezes (`HALF_EVEN` em 34 dígitos seguido do modo alvo).

## D2 · Hierarquia verdadeiramente selada em Java 8 (`public abstract class Dinheiro`)

- **Decision**: Modelar `Dinheiro` como `public abstract class` com construtor `package-private`, estendida exclusivamente pelas quatro classes `final` do pacote (`Real`, `Dolar`, `Euro` e `QuantiaMonetaria`). `Dinheiro.de(...)` devolve canonicamente `Real` para `("BRL", 2)` (fixando `Moeda.BRL`), `Dolar` para `("USD", 2)` (`Moeda.USD`), `Euro` para `("EUR", 2)` (`Moeda.EUR`) e `QuantiaMonetaria` para qualquer outro par `(codigo, casasDecimais)`.
- **Rationale**: Em Java 8 (sem `sealed interface`), uma `interface` pública permitiria implementações externas mutáveis quebrando a simetria de `equals` e impediria adicionar métodos a `Dinheiro` em specs futuras sem acusar `METHOD_ADDED_TO_INTERFACE` no `japicmp`. Uma `abstract class` com construtor `package-private` sela completamente os subtipos no pacote, centraliza campos e `equals`/`hashCode`/`toString` (DRY) e mantém todas as fábricas estáticas e retornos covariantes.
- **Alternatives considered**: `public interface Dinheiro` — rejeitada na revisão independente pelos riscos de quebra de simetria e evolução incompatível de interface.

## D3 · Snapshot único (anti-TOCTOU), ausência de ciclos `<clinit>` e limites anti-DoS

- **Decision**:
  1. Em `MoedaPadrao` e `AritmeticaMonetaria`, realizar toda validação lexical por laços diretos de caracteres (`charAt`), **sem `Pattern` estático**, eliminando qualquer risco de ciclo de inicialização de classes (JLS §12.4.2) entre `Moeda` (que possui método `default` e constantes `BRL`, `USD`, `EUR`), `MoedaPadrao` e `Real.ZERO`.
  2. Capturar `getCodigo()`, `getCasasDecimais()` e `getSimbolo()` de uma `Moeda` externa **uma única vez** em variáveis locais antes de validar e instanciar `MoedaPadrao` (prevenindo TOCTOU).
  3. Impor limites públicos explícitos: textos de entrada com no máximo `64` caracteres; `BigDecimal` de entrada (`valor`, `fator`, `cotacao`, `peso`) com `precision() <= 38` e `scale()` no intervalo `[-18, 38]`; soma ou produto monetário resultante dentro de `38` dígitos significativos.
- **Rationale**: Evita `NullPointerException` em `<clinit>`, corrige vulnerabilidade TOCTOU em implementações maliciosas de `Moeda` e impede negação de serviço por `BigDecimal("1E-999999999")` ou equalização de escalas gigantes em `alocar`.
- **Alternatives considered**: `BigDecimal` ilimitado — rejeitado pelo custo potencial de memória/CPU em `setScale` e `BigInteger` com expoentes extremos.

## D4 · Política retida, propagação determinística e igualdade contábil

- **Decision**: `equals` e `hashCode` de `Dinheiro` são `final` na classe base e comparam apenas `(moeda.codigo, moeda.casasDecimais, valor)`. Todas as operações unárias (`abs`, `negar`), binárias (`somar`, `subtrair`), de escala (`multiplicar(long)`, `multiplicar(BigDecimal)`, `dividir(long)`), de distribuição (`ratear`, `alocar`) e `converter(TaxaCambio)` preservam o `MetodoArredondamento` do receptor; as sobrecargas que recebem explicitamente `MetodoArredondamento` (`multiplicar(fator, arred)`, `dividir(divisor, arred)`, `converter(taxa, arred)`, `comArredondamento(arred)`) aplicam e **retêm** no resultado a política informada.
- **Rationale**: Define sem ambiguidade a política resultante em 100% das operações e mantém igualdade contábil previsível em coleções.
- **Alternatives considered**: Incluir `arredondamento` em `equals` — rejeitado por quebrar equivalência de valores monetários idênticos.

## D5 · Rateio e alocação proporcional exata (Hamilton com `BigInteger`)

- **Decision**: `ratear` usa `divideAndRemainder` sobre unidades mínimas inteiras (`BigInteger`) com `PRIMEIRAS_PARCELAS` ou `ULTIMAS_PARCELAS`. `alocar` equaliza a escala dos pesos (respeitando os limites de precisão/escala de D3 e até `10_000` elementos) e aplica `(|U| * w_i)` dividido por `S` em `BigInteger` exato: um peso `w_i == 0` tem numerador `0` e resto `0`, portanto **jamais** recebe unidade residual.
- **Rationale**: Garante exatidão matemática absoluta sem ponto flutuante nem truncamento de dízima.
- **Alternatives considered**: Divisão decimal aproximada — rejeitada por potencial ruído em empates exatos.

## D6 · `TaxaCambio` exata, involutiva e conversão bidirecional nos atalhos tipados

- **Decision**: `TaxaCambio` rejeita `mesmaMoeda(origem, destino)` e armazena internamente `(moedaBase, moedaCotada, cotacaoBase, invertida)`, normalizando `cotacaoBase` sem gerar escala negativa (`stripped.scale() < 0 ? stripped.setScale(0) : stripped`, evitando `"1E+2"`). `inverter()` apenas alterna a flag `invertida` sem dividir `1 / cotacao`, tornando `t.inverter().inverter().equals(t)` **exato e involutivo**! Na conversão inversa, divide-se o montante diretamente por `cotacaoBase` usando a rotina de divisão exata de D1 (sem arredondamento intermediário). `TaxaCambio.converter(Dinheiro)` exige `valor.moeda == taxa.moedaOrigem`, enquanto `real.paraDolar(taxa)`, `real.paraEuro(taxa)`, `dolar.paraReal(taxa)`, etc., aceitam `TaxaCambio` em qualquer uma das duas direções do par esperado (ex.: `USD -> BRL` da PTAX direto em `real.paraDolar(ptax)`!).
- **Rationale**: Resolve completamente a perda de 1 centavo na volta com `TRUNCAMENTO`/`PISO` e oferece ergonomia natural para cotações PTAX brasileiras sem abrir mão de segurança direcional.
- **Alternatives considered**: `ONE.divide(cotacao, DECIMAL128)` em `inverter()` — rejeitado por perder involução e introduzir arredondamento intermediário.

## D7 · Extensões e operadores Kotlin completos

- **Decision**: Prover extensões e operadores sobre `Dinheiro`, `Real`, `Dolar`, `Euro` e `QuantiaMonetaria` em `Financas.kt`, incluindo multiplicação comutativa (`2 * real` e `real * 2`), `+`/`-` tipados também para `QuantiaMonetaria` e `Dinheiro` (`somarMesmaMoeda`), `alocar(vararg pesos: Long)` e construtores explícitos (`"10.50".reais()`, `1050L.centavosReais()`, `10L.reaisInteiros()`), evitando ambiguidade com centavos.
- **Rationale**: Máxima ergonomia Kotlin sem *boxing* de `value class` sob herança de classe abstrata e zero confusão entre unidades principais e centavos.
- **Alternatives considered**: `Long.reais()` curto — substituído por `reaisInteiros()` / `centavosReais()` para evitar que `1050L.reais()` seja confundido com `R$ 10,50` em integrações CNAB.
