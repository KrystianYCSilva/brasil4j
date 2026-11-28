# Guia de verificação · Spec 010

## Uso por contrato

```java
DocumentParser<Cpf> cpf = Cpf.parser(CpfValidationMode.NUMERIC);
ValidationResult<Cpf> result = cpf.parse("28001238939");
result.getRejection().map(Rejection::getReason);          // Optional[INVALID_CHECK_DIGITS]

String masked = Cnpj.parser(CnpjValidationMode.FORMAT_ONLY)
        .parse("12ABC34501DE36")
        .map(Cnpj::applyMask)
        .orElseThrow();                                    // 12.ABC.345/01DE-36
```

## NIS com validador opcional

```java
Nis.parser().isValid("01234567898");                                    // true: só forma
Nis.parser(NisValidationMode.CAIXA_FGTS_V016).isValid("00000000060");   // false: resto 1
Nis.parser(NisValidationMode.LEGACY_MOD11).isValid("00000000060");      // true

DocumentParser<Nis> caixaComGuarda = Nis.parser(NisValidationMode.CAIXA_FGTS_V016)
        .withRule(NisRules.repeatedDigits());                             // guarda SIPREV RV_03, opcional
caixaComGuarda.isValid("00000000000");                                   // false
caixaComGuarda.isValid("11111111116");                                   // true
```

## Documento do consumidor

```java
final class Matricula extends AbstractDocument implements MaskedDocument {
    private static final DocumentMask MASK = DocumentMask.of("999.999-999");
    static final DocumentParser<Matricula> PARSER = DocumentParsers.builder(Matricula::new)
            .mask(MASK)
            .checkDigits(Modulus11.builder().weightsFromRight(2, 9).digits(1).build())
            .errorMessage("Matrícula inválida.")
            .build();
    private Matricula(String canonical) { super(canonical); }
    public String applyMask() { return MASK.apply(getCanonicalValue()); }
}

List<DocumentCheck<?>> itens = Arrays.asList(
        DocumentCheck.of("123.456-789", Matricula.PARSER),   // base 12345678, soma 156, resto 2, DV 9
        DocumentCheck.cpf("01234567898", CpfValidationMode.FORMAT_ONLY));
```

## Kotlin

```kotlin
val cpf: Cpf? = "28001238939".toCpfOrNull(CpfValidationMode.NUMERIC)   // null
val nis: Nis? = "00000000019".toNisOrNull(NisValidationMode.CAIXA_FGTS_V016)
```

## Comandos

```powershell
.\mvnw.cmd -B verify
```

```powershell
python scripts/check-api-compatibility.py <baseline-v0.2.0> . <japicmp.jar> $HOME\.m2\repository
```

Rodar o `verify` em JDK 8 e em JDK 21. Para a compatibilidade, reconstruir `v0.2.0`, `5fd9dae` e `18bfe9c` em diretórios separados, com o mesmo JDK 21 do candidato. Para a proveniência, buscar "oficial" em `brasil4j-*/src` e `docs` e conferir que cada ocorrência remete a `docs/fontes.md`.
