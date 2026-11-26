# Quickstart: CPF

Execute `mvnw.cmd clean verify` no Windows ou `sh ./mvnw clean verify` em Unix com JDK 8 e JDK 21. Os testes exercitam os dois modos e a saída protegida pelos JARs.

## Java

```java
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.cpf.CpfValidator;

Cpf cpf = Cpf.parse("280.012.389-39");
boolean dvValido = CpfValidator.isValid("28001238939", CpfValidationMode.NUMERIC);  // false
String texto = cpf.toString();  // ***.***.***-**
```

## Kotlin

```kotlin
import io.github.brasil4j.identidade.cpf.CpfValidationMode.NUMERIC
import io.github.brasil4j.kotlin.identidade.toCpf

val cpf = "280.012.389-38".toCpf(NUMERIC)
check(cpf.hasValidCheckDigits())
```

As operações explícitas de valor canônico e `applyMask()` exibem os dígitos. Consulte o [contrato](contracts/api.md).
