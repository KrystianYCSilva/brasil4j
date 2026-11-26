# Quickstart: CNPJ

Execute `mvnw.cmd clean verify` no Windows ou `sh ./mvnw clean verify` em Unix com JDK 8 e JDK 21. A matriz do C1 roda pelos JARs em Java e Kotlin.

## Java

```java
import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.cnpj.CnpjValidator;

Cnpj empresa = Cnpj.parse("12.ABC.345/01DE-35");
String base = empresa.getBase();  // 12ABC34501DE
boolean formatoValido = CnpjValidator.isValid("12ABC34501DE36", CnpjValidationMode.FORMAT_ONLY);
```

## Kotlin

```kotlin
import io.github.brasil4j.kotlin.identidade.toCnpj

val empresa = "12.ABC.345/01DE-35".toCnpj()
check(empresa.checkDigits == "35")
```

Consulte as operações e os limites no [contrato](contracts/api.md).
