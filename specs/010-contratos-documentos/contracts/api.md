# Contrato público planejado · Spec 010

Todas as adições são públicas e compatíveis com Java 8. As assinaturas existentes de CPF, CNPJ, NIS e `DocumentCheck` são preservadas.

## `io.github.brasil4j.identidade`

```java
public interface Document { String getCanonicalValue(); }

public interface MaskedDocument extends Document { String applyMask(); }

public interface SecurityMaskedDocument extends Document {
    String applySecurityMask();                     // caractere '*'
    String applySecurityMask(char maskChar);        // IAE se dígito ou letra
}

public interface CheckDigitDocument extends Document {
    String getBase();
    String getCheckDigits();
    boolean hasValidCheckDigits();
}

public abstract class AbstractDocument implements Document {
    protected AbstractDocument(String canonical);   // NPE se nulo
    public String getCanonicalValue();
    public boolean equals(Object other);            // getClass() igual e canônico igual; não final (japicmp METHOD_NOW_FINAL)
    public int hashCode();
    public String toString();                       // "[documento oculto]"
}

public enum RejectionReason { MISSING, INVALID_FORMAT, INVALID_CHECK_DIGITS, UNSUPPORTED_BASE, POLICY }

public final class Rejection {
    public static Rejection of(RejectionReason reason);
    public static Rejection of(RejectionReason reason, String code); // código não nulo e não vazio
    public RejectionReason getReason();
    public Optional<String> getCode();
    // equals/hashCode por motivo e código; toString "Rejection[REASON]" ou "Rejection[REASON:CODE]"
}

public abstract class ValidationResult<D> {
    ValidationResult();                              // package-private
    public static <D> ValidationResult<D> valid(D document);
    public static <D> ValidationResult<D> invalid(Rejection rejection);
    public abstract boolean isValid();
    public abstract Optional<D> getDocument();
    public abstract Optional<Rejection> getRejection();
    public abstract <U> ValidationResult<U> map(Function<? super D, ? extends U> mapper);
    public abstract <R> R fold(Function<? super D, ? extends R> onValid,
                               Function<? super Rejection, ? extends R> onInvalid);
    public D orElseThrow();                          // IllegalArgumentException("Documento inválido.")
    public D orElseThrow(String message);            // IllegalArgumentException(message)
    public <X extends Throwable> D orElseThrow(Function<? super Rejection, ? extends X> exception) throws X;
    // toString "ValidationResult[VALID]" ou "ValidationResult[INVALID:Rejection[REASON]]" (ou "...[REASON:CODE]]")
}

@FunctionalInterface
public interface ValidationRule<T> {
    Optional<Rejection> check(T value);
    static <T> ValidationRule<T> of(Predicate<? super T> condition, Rejection rejection);
    static <T> ValidationRule<T> accept();
    static ValidationRule<String> checkDigits(CheckDigitAlgorithm algorithm); // INVALID_CHECK_DIGITS ou INVALID_FORMAT fora do domínio
    default ValidationRule<T> and(ValidationRule<? super T> next);
}

@FunctionalInterface
public interface DocumentParser<D extends Document> {
    ValidationResult<D> parse(String input);
    default D parseOrThrow(String input);            // IllegalArgumentException("Documento inválido.")
    default boolean isValid(String input);
    default DocumentParser<D> withRule(ValidationRule<? super D> rule);
    default DocumentParser<D> withPolicy(Predicate<? super D> policy);
    default DocumentParser<D> withPolicy(Predicate<? super D> policy, String code);
}

public final class DocumentParsers {
    public static <D extends Document> Builder<D> builder(Function<String, D> factory);
    public static final class Builder<D extends Document> {
        public Builder<D> mask(DocumentMask mask);
        public Builder<D> normalizer(Function<String, Optional<String>> normalizer);
        public Builder<D> canonicalRule(ValidationRule<String> rule);
        public Builder<D> checkDigits(CheckDigitAlgorithm algorithm);
        public Builder<D> documentRule(ValidationRule<? super D> rule);
        public Builder<D> errorMessage(String message);
        public DocumentParser<D> build();            // IllegalStateException sem mask nem normalizer
    }
}

@FunctionalInterface
public interface DocumentFormatter<D extends Document> extends Function<D, String> {
    static <D extends Document> DocumentFormatter<D> canonical();
    static <D extends MaskedDocument> DocumentFormatter<D> masked();
    static <D extends Document> DocumentFormatter<D> redacted(int visiblePrefix, int visibleSuffix, char maskChar);
    static <D extends SecurityMaskedDocument> DocumentFormatter<D> secure();
    static <D extends SecurityMaskedDocument> DocumentFormatter<D> secure(char maskChar);
}
```

O builder rejeita nulos com NPE e rejeita configurar tanto `mask` quanto `normalizer`. O parser construído por ele trata `null` e `""` como `MISSING`. Uma normalização vazia vira `INVALID_FORMAT`. As regras canônicas rodam em ordem, seguidas do DV (`INVALID_CHECK_DIGITS`), da fábrica do documento e das regras de documento. Os interpretadores nativos sobrescrevem a mensagem por `errorMessage`. Um `parseOrThrow` de interpretador nativo usa essa mensagem por meio de uma implementação interna que sobrescreve o método default.

`ValidationRule.checkDigits` converte `InvalidBaseException` e qualquer outra `IllegalArgumentException` (argumento inadequado) em `INVALID_FORMAT`. Outras exceções propagam-se. Algoritmos devem usar `InvalidBaseException` para recusar uma base fora do domínio e `IllegalStateException` (objeto em estado inadequado) para defeitos de configuração.

`withPolicy(policy)` rejeita com `Rejection.of(POLICY)` e `withPolicy(policy, code)` com `Rejection.of(POLICY, code)`. As decorações preservam a mensagem de erro do interpretador de origem. As decorações são achatadas: a origem não decorada é interpretada uma única vez por chamada, e as regras rodam na ordem em que foram acrescentadas. Em `parseOrThrow`, a rejeição da origem mantém a exceção dela, inclusive a de um interpretador do consumidor, em qualquer profundidade. A rejeição por regra usa a mensagem da origem configurada, ou "Documento inválido." para interpretadores do consumidor. `redacted` exige `visiblePrefix ≥ 0` e `visibleSuffix ≥ 0`. Quando a soma é maior ou igual ao comprimento do canônico, oculta tudo.

## `io.github.brasil4j.verificacao`

```java
public interface CheckDigitAlgorithm {
    int digitCount();
    Optional<String> compute(String base);           // vazio quando a base não admite DV válido
    default boolean verify(String canonical);        // compara os últimos digitCount() caracteres
}

public final class Modulus11 implements CheckDigitAlgorithm {
    public static Builder builder();
    public static final class Builder {
        public Builder charValue(IntUnaryOperator charToValue, String alphabet); // padrão: ASCII − 48, "0123456789"
        public Builder weightsFromRight(int first, int last);  // cíclicos da direita: first..last, recomeça em first
        public Builder weightsFromLeft(int... weights);         // pesos fixos da esquerda para a direita
        public Builder remainder(IntFunction<OptionalInt> mapping); // resto → DV; vazio = base sem DV válido
        public Builder digits(int count);                       // padrão 1; cada DV intermediário entra na base do seguinte
        public Modulus11 build();
    }
    public static Modulus11 cpf();
    public static Modulus11 cnpj();
    public static Modulus11 pisCaixaFgtsV016();
    public static Modulus11 pisLegacyMod11();
}
```

O mapeamento de resto padrão é `r < 2 → 0, senão 11 − r`. Os pesos padrão são `weightsFromRight(2, 9)`. `compute` e `verify` lançam `NullPointerException` para nulo e `InvalidBaseException` para base vazia, maior que os pesos fixos ou com caractere fora do alfabeto. Em `verify`, só a base é conferida contra o domínio. Um sufixo divergente, inclusive não numérico, devolve `false`. Cada parcela é reduzida módulo 11, o que evita transbordamento para qualquer peso aceito. Os presets são instâncias imutáveis compartilhadas.

O alfabeto pode conter apenas um subconjunto dos dígitos decimais. O `build()` avalia o mapeamento do resto para os restos de 0 a 10 (a função deve ser pura e determinística) e lança `IllegalArgumentException` se algum resultado for nulo ou fora de 0 a 9 e, com vários DVs, se algum dígito produzido não pertencer ao alfabeto, pois os DVs anteriores ao último voltam à base e o mesmo mapeamento gera todos. Com um único DV, o dígito pode estar fora do alfabeto da base. Como o build garante o mapeamento, o cálculo não lança `IllegalStateException` por esse motivo.

- `cpf()`: `weightsFromRight(2, 11)` com 2 DVs. O peso não recomeça, porque a base tem no máximo 10 caracteres.
- `cnpj()`: alfabeto `0-9A-Z` com ASCII − 48, `weightsFromRight(2, 9)` e 2 DVs.
- `pisCaixaFgtsV016()`: `weightsFromLeft(3,2,9,8,7,6,5,4,3,2)`, resto `0 → 0`, `1 → vazio`, senão `11 − r`, 1 DV.
- `pisLegacyMod11()`: os mesmos pesos, com o mapeamento padrão, 1 DV.

## `io.github.brasil4j.mascara`

```java
public final class DocumentMask {
    public static DocumentMask of(String pattern);   // 9 dígito, A letra A-Z, X A-Z ou 0-9
    public int canonicalLength();
    public Optional<String> unmask(String input);    // canônico conforme os slots ou máscara completa exata
    public String apply(String canonical);           // IAE se não conforme
    public String applySecure(String canonical, int hiddenPrefix, int hiddenSuffix, char maskChar);
}
```

`applySecure` aplica a máscara e substitui pelo caractere os `hiddenPrefix` primeiros e os `hiddenSuffix` últimos slots, preservando os literais. Quantidades negativas e caracteres que sejam dígito ou letra geram IAE. Uma soma maior ou igual ao número de slots oculta todos os slots.

## Documentos

| Tipo | Adições |
| --- | --- |
| Máscara de segurança | `Cpf`, `Cnpj` e `Nis` implementam `SecurityMaskedDocument`. Layout: CPF oculta 3 + 2 (`***.345.678-**` para `01234567890`), CNPJ oculta 2 + 2 (`**.ABC.345/01DE-**`), NIS oculta 3 + 1 sobre o canônico sem pontuação (`***3456789*` para `01234567898`). O layout é uma escolha de projeto, registrada em `docs/fontes.md` sem rótulo de "oficial". Sem ocultação: `applyMask()` em CPF e CNPJ (inalterado) e `getCanonicalValue()` no NIS, que não implementa `MaskedDocument`. |
| `Cpf` | `extends AbstractDocument implements MaskedDocument, CheckDigitDocument`. `static DocumentParser<Cpf> parser()` e `parser(CpfValidationMode)` (IAE para modo nulo). `toString` continua `***.***.***-**`. |
| `Cnpj` | `extends AbstractDocument implements MaskedDocument, CheckDigitDocument`. `parser()`, `parser(CnpjValidationMode)` e `hasValidCheckDigits()` (falso para 14 zeros). `toString` continua canônico. |
| `Nis` | `extends AbstractDocument`. `parser()` e `parser(NisValidationMode)`. `toString` continua `[NIS oculto]`. |
| `NisValidationMode` | `FORMAT_ONLY`, `CAIXA_FGTS_V016`, `LEGACY_MOD11`. O JavaDoc cita fonte e alcance. |
| `NisRules` | `static ValidationRule<Nis> repeatedDigits()` rejeita onze algarismos iguais com `Rejection.of(INVALID_CHECK_DIGITS, "REPEATED_DIGITS")`. A fonte é a RV_03 do SIPREV, com alcance no SIPREV. `static ValidationRule<Nis> checkDigits(CheckDigitAlgorithm)` aplica qualquer algoritmo ao canônico. As duas regras são encadeáveis por `withRule`. |
| `DocumentCheck<T>` | `static <D extends Document> DocumentCheck<D> of(String input, DocumentParser<D> parser)`. NPE para parser nulo. |
| `DocumentCheckResult` | `Optional<Rejection> getRejection()`, presente nas duas rejeições. Uma rejeição pela política do item usa `Rejection.of(POLICY)`. |

## Fachadas deprecadas

`CpfValidator`, `CpfFormatter`, `CnpjValidator`, `CnpjFormatter`, `NisValidator` e `NisFormatter` ganham `@Deprecated` e `@deprecated`, com o substituto indicado:

- `CpfValidator.isValid(input, mode)` → `Cpf.parser(mode).isValid(input)`
- `CpfFormatter.format(input)` → `Cpf.parse(input).applyMask()`
- `CnpjValidator` e `CnpjFormatter` seguem o mesmo padrão
- `NisValidator.isValid(input, policy)` → `Nis.parser().withPolicy(policy).isValid(input)`
- `NisFormatter.format(nis, f)` → `f.apply(nis)`

O comportamento das fachadas não muda.

## Kotlin · `io.github.brasil4j.kotlin.identidade`

```kotlin
fun String?.toCpfOrNull(mode: CpfValidationMode = CpfValidationMode.FORMAT_ONLY): Cpf?
fun String?.toCnpjOrNull(mode: CnpjValidationMode = CnpjValidationMode.ALPHANUMERIC): Cnpj?
fun String?.toNisOrNull(mode: NisValidationMode = NisValidationMode.FORMAT_ONLY): Nis?
fun <D : Document> DocumentParser<D>.parseOrNull(input: String?): D?
fun String.toNis(mode: NisValidationMode): Nis
// Cnpj.hasValidCheckDigits(): Boolean
// Cpf, Cnpj e Nis: applySecurityMask(maskChar: Char = '*'): String
```
