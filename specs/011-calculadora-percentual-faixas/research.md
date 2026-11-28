# Research · Spec 011 — Calculadora financeira, percentuais e tabela progressiva

## D1 · `CalculadoraFinanceira` com arredondamento monetário final e teto de `MathContext`

- **Decision**: Expor `@FunctionalInterface CalculoFinanceiro` e `public final class CalculadoraFinanceira` (`PADRAO`, `de(arred)`, `de(arred, mathContext)` com `mathContext.precision` em `1..90` e `roundingMode != UNNECESSARY`). A calculadora avalia o intermediário de precisão finita e aplica um único arredondamento monetário final para a escala da moeda alvo. No validador do retorno intermediário (`precision <= 180`, `scale` em `[-60, 180]`), verifica-se primeiro `scale()` e `unscaledValue().bitLength() <= 600` antes de `precision()` para evitar custo de contagem decimal em inteiros gigantes. Exceções lançadas dentro de `CalculoFinanceiro` propagam-se sem embrulho.
- **Rationale**: Limitar `MathContext` a `90` garante que produtos de dois operandos intermediários caibam no teto `180`. Documentar que o `MathContext` intermediário realiza seus próprios arredondamentos evita falsa expectativa em modos direcionais (`TRUNCAMENTO`/`PISO`/`TETO`) sobre dízimas como `100 * (1/3) * 3 = 99.999...9`.
- **Alternatives considered**: `MathContext` ilimitado ou `precision > 90` — rejeitado por viabilizar DoS ou produzir intermediários que estourariam o teto de `180` dígitos na saída.

## D2 · `Percentual` com invariante sobre o fator canônico (`0..40`) e assinatura genérica enxuta

- **Decision**:
  1. Validar a invariante diretamente sobre o **fator canônico** (`precision <= 38`, `scale` em `0..40`, `stripTrailingZeros` com `scale >= 0`), tanto nas fábricas (`de(String)`, `de(BigDecimal)`, `de(long)`, `deFator(...)`, `dePontosBase(long)`) quanto em `somar`/`subtrair`/`negar`. Multiplicar valores monetários pelo fator por rotina interna dedicada em `AritmeticaMonetaria`, aceitando escala até `40` sem rejeitar `Percentual.de("0.<38 casas>")`.
  2. Expor métodos genéricos `<T extends Dinheiro> T aplicarSobre/acrescentarEm/descontarDe/calcularBasePorDentro` apoiados em método `package-private abstract Dinheiro criarMesmoSubtipo(BigDecimal, MetodoArredondamento)` em `Dinheiro` (8 métodos públicos em vez de 40 sobrecargas, mantendo `japicmp` enxuto).
  3. Garantir `acrescentarEm(v) = v + aplicarSobre(v)` e `descontarDe(v) = v - aplicarSobre(v)` e `calcularBasePorDentro(L) = dividirExato(L, 1 - f)` para `f` em `[0, 1)`, cuja reversibilidade `descontarDe(calcularBasePorDentro(L, m), m) == L` é exata nos sete modos padrão (pois `|e(1 - f) - d| < 1` unidade mínima).
- **Rationale**: Elimina explosão combinatória de sobrecargas e garante fechamento contábil incondicional nos modos padrão.
- **Alternatives considered**: 40 sobrecargas concretas — rejeitado por poluição de API e KDoc.

## D3 · `TabelaProgressiva<T extends Dinheiro>` com intervalos `(L_{k-1}, L_k]` e memória reconciliada

- **Decision**:
  1. Definir intervalos `(L_{k-1}, L_k]` (`[0, L_1]` na 1ª faixa), `Builder<T>` de uso único (selado após `faixaAcima`, lançando `IllegalStateException` em reuso), validação de moeda em tempo de execução para `paraMoeda(Moeda)` e alíquotas `>= 0%` (habilitando tabelas com teto via `faixaAcima(Percentual.ZERO)`, documentando que nesse caso `parcelaADeduzirExata` da faixa final pode ser negativa).
  2. Em `ApuracaoProgressiva<T>`, listar todas as faixas da tabela (com `baseNaFaixa = 0` nas faixas não atingidas), manter `valorExatoTotal` sempre igual à soma exata dos produtos das faixas e garantir `soma(faixa.getValorApurado()) == valorTotal` em ambos os modos: em `POR_FAIXA`, cada faixa é arredondada individualmente; em `NO_TOTAL`, `valorTotal = round(valorExatoTotal, arred)` e os `valorApurado` por faixa são reconciliados por maiores restos (Hamilton) sobre os valores exatos de cada faixa.
  3. Classificar ambos os modos puramente como convenções matemáticas de decomposição marginal (`CONVENCAO_TECNICA`), sem atribuir comportamento normativo a tributos específicos sem fonte oficial citada.
- **Rationale**: Preserva integridade contábil da memória de cálculo em 100% dos casos e respeita rigorosamente o Princípio V de proveniência.
- **Alternatives considered**: Deixar `soma(faixa.getValorApurado()) != valorTotal` em `NO_TOTAL` — rejeitado por quebrar reconciliação de demonstrativos linha a linha.
