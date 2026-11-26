# Quickstart: IBGE e UF

Para validar a implementação, execute `mvnw.cmd -B -ntp clean verify` em JDK 8 e 21, cada um com seu `JAVA_HOME`. Confira JaCoCo, DocLint, Dokka em JDK 21 e bytecode 52. Registre a comparação japicmp dos dois JARs da v0.2.0 contra a v0.1.0.

Exercite `3550308`, `4115200`, `4113700` e `3509502` pelos dois JARs. Em Java, use `CodigoMunicipio.parse("3550308").matchesUf(Uf.SP)`. Em Kotlin, use `"4115200".toCodigoMunicipio().uf`. Para uma sigla exata, o enum Java oferece `Uf.valueOf("SP")`, que diferencia maiúsculas de minúsculas e lança `IllegalArgumentException` para um nome desconhecido. O tratamento de sigla externa fica no consumidor.

Na borda atual do Censo, a entrada já chega como `Long`. Use `String.valueOf(long)` na sondagem do adaptador e preserve a conversão do framework. Os quatro códigos canônicos fazem ida e volta. `0012345` chega a `12345` no caminho numérico. `+4115200` e `04115200` ilustram entradas rejeitadas pelo parser textual, cuja forma original se perde depois de `Long.parseLong`. A sondagem Java não demonstra a aceitação HTTP dessas formas. O cadastro mantém a conferência de país, existência e fonte.

## Dados da UF

```java
Uf uf = Uf.fromName("  sao paulo  ").orElseThrow(IllegalArgumentException::new);
uf.getNome();     // São Paulo
uf.getSigla();    // SP
uf.getCapital();  // São Paulo
uf.getIbgeCode(); // 35
uf.getDdds();     // 11 a 19, em ordem
```

```kotlin
import io.github.brasil4j.geografia.Uf
import io.github.brasil4j.kotlin.geografia.toCodigoMunicipio

val uf = Uf.fromName("  goias  ").get()
val capital = uf.capital // Goiânia
val ddds = uf.ddds       // [61, 62, 64]
val corresponde = "5208707".toCodigoMunicipio().matchesUf(uf)
```

O DDD `61` é compartilhado por GO e DF. A busca por nome completo ignora caixa, acentos e espaços nas extremidades. `Uf.fromIbgeCode` exige o código exato.
