# Contrato público · Spec 014

## Java · `io.github.brasil4j.trabalhista.ctps`

| Tipo | Assinaturas |
| --- | --- |
| `public enum CtpsProfile` | Constante `ESOCIAL_S2200_V2_5` e método `CtpsCheckResult check(CtpsGroup group)`. |
| `public final class CtpsGroup` | `public static CtpsGroup of(String number, String series, String uf, CtpsOrigin origin, boolean sourceLoss)`. |
| `public enum CtpsOrigin` | `PHYSICAL`, `CPF_PROJECTION`, `UNDETERMINED`. |
| `public final class CtpsCheckResult` | `CtpsProfile getProfile()`, `Status getStatus()`, `Set<Field> getAffectedFields()`, `Optional<CtpsOrigin> getOrigin()`, `boolean hasSourceLoss()`, `Optional<CtpsRecord> getRecord()`, `Optional<Uf> getIssuingUf()`. |
| `CtpsCheckResult.Status` | `ABSENT`, `INCOMPLETE`, `OUT_OF_PROFILE`, `CONFORMING`. |
| `CtpsCheckResult.Field` | `NUMBER`, `SERIES`, `UF`. |
| `public final class CtpsRecord` | `String getNumber()`, `String getSeries()`, `Uf getUf()`. `Uf` vem de `io.github.brasil4j.geografia`. |

### Entradas, resultados e falhas

- `check(null)` representa grupo ausente. `CtpsGroup.of` cria um grupo presente e aceita campos nulos ou vazios. Origem nula lança `NullPointerException` com mensagem fixa e sem dados de entrada. Use `UNDETERMINED` quando a origem for desconhecida.
- Os estados seguem a [precedência do modelo](../data-model.md#decisão-e-precedência). O conjunto de campos afetados é imutável, sem duplicatas, com iteração na ordem `NUMBER`, `SERIES`, `UF`. Os quatro resultados esperados são devolvidos sem exceção.
- `getOrigin()` retorna vazio apenas em `ABSENT`, cujo `hasSourceLoss()` é `false`. Nos outros estados, origem e indicação de perda são as declaradas.
- `getRecord()` é presente apenas em `CONFORMING`. `getIssuingUf()` exige `CONFORMING` com origem `PHYSICAL`. Nas outras origens, a UF do registro tem somente o significado de UF informada no leiaute.
- Os `Optional` e o conjunto retornados são não nulos. Grupo, registro e resultado são imutáveis e têm construção controlada. O grupo usa o marcador `[Grupo CTPS oculto]` e o registro usa `[Registro CTPS oculto]`. A do resultado pode expor perfil, estado e nomes de campos afetados. Número, série e UF fornecidos ficam fora de `toString()` e das mensagens da biblioteca.
- O motor recebe o texto já decodificado, após a extração. A conformidade cobre as facetas do perfil. Bem-formação XML, inclusive sequências que XML 1.0 não admite, é responsabilidade do extrator ou da origem dos dados.

## Kotlin · `io.github.brasil4j.kotlin.identidade`

O consumidor constrói o grupo com os tipos Java e recebe o mesmo resultado.

```kotlin
public fun CtpsGroup?.checkCtps(profile: CtpsProfile): CtpsCheckResult
public val CtpsCheckResult.recordOrNull: CtpsRecord?
public val CtpsCheckResult.originOrNull: CtpsOrigin?
public val CtpsCheckResult.issuingUfOrNull: Uf?
```

`checkCtps` delega ao perfil escolhido explicitamente. As propriedades adaptam `Optional.empty()` para `null`. Estado, campos afetados, perda e diagnósticos seguem o contrato Java.

## Identificador da CTPS Digital

Em Java, usar `Cpf.parser(mode).parse(input)` com `CpfValidationMode.FORMAT_ONLY` ou `NUMERIC`. Em Kotlin, usar `input.toCpfOrNull(mode)` ou o mesmo interpretador Java para obter o motivo da rejeição. O modo determina se o DV integra a conferência. A apresentação ao empregador, a emissão, a titularidade e o vínculo pertencem ao fluxo consumidor. A projeção legada `7+4` é uma origem declarada de grupo histórico.
