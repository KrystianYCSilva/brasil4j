# Quickstart · Verificar a entrega da Spec 006

Este guia descreve a execução esperada após a implementação. Use apenas os vetores sintéticos documentados.

## Consumo da API

```java
import io.github.brasil4j.identidade.nis.Nis;

Nis nis = Nis.parse("00000000019");
String campo = nis.getCanonicalValue(); // 00000000019
String diagnostico = nis.toString();   // [NIS oculto]
```

```kotlin
import io.github.brasil4j.kotlin.identidade.Nis
import io.github.brasil4j.kotlin.identidade.toNis

val extraido: String? = "00000000019"
val nis = Nis(extraido)
val equivalente = "00000000019".toNis()
check(nis == equivalente)
check(nis.toJava().canonicalValue == "00000000019")
val emDiagnostico: Any = nis
check("$emDiagnostico" == "[NIS oculto]")
```

## Compor regras e apresentações do consumidor

Quem mantém uma regra própria pode adaptá-la ao valor canônico. Na UEM, por exemplo, o adaptador pode chamar `ValidationUtils.isPis(valor.getCanonicalValue())` sobre o `Nis` recebido. A conferência da UEM continua uma política daquele consumidor.

```java
import io.github.brasil4j.identidade.nis.NisFormatter;
import io.github.brasil4j.identidade.nis.NisValidator;
import java.util.function.Predicate;

Predicate<Nis> regraLocal = valor -> valor.getCanonicalValue().startsWith("0");
boolean formaValida = NisValidator.isValid(campo);
boolean aceito = NisValidator.isValid(campo, regraLocal);
String rotulo = NisFormatter.format(nis, valor -> "inscrição:" + valor.getCanonicalValue());
```

A regra acima é sintética. Combine regras de um mesmo tipo com `Predicate.and` e apresentações com `Function.andThen`. Para percorrer CPF, CNPJ e NIS na mesma lista, vincule cada operação ao seu tipo na montagem da lista. Por exemplo, em Java, reúna as checagens como `List<BooleanSupplier>` e as apresentações como `List<Supplier<String>>`.

```kotlin
import io.github.brasil4j.kotlin.identidade.isValidNis

val formaValida = extraido.isValidNis()
val aceito = extraido.isValidNis { it.canonicalValue.startsWith("0") }
val rotulo = nis.format { "inscrição:" + it.canonicalValue }
```

A política roda somente para entradas estruturais. O valor produzido pela função de apresentação pode conter os dígitos. Use a saída padrão oculta para diagnósticos.

## Conferência dos fragmentos de S-2200

Java e Kotlin devem ler os mesmos recursos de teste. O adaptador de cada teste extrai `nisTrab` conforme o namespace histórico e entrega o campo à API pública do JAR.

| Campo extraído | Resultado esperado |
| --- | --- |
| `00000000019` e `01234567897` | Aceitar cada valor e recuperar exatamente o campo correspondente. |
| `1234567897` e `A1234567897` | Rejeitar, mesmo sendo representáveis pelo fragmento XSD local inspecionado. |
| `01234567898` | Aceitar por forma, ainda que o valor divirja do DV do legado. |
| `012.34567.89-7` e `01234567897` com um espaço em cada extremidade | Rejeitar a máscara e os espaços preservados durante a extração. |
| Campo com quebra de linha ou tabulação, campo só com espaços | Preservar esses caracteres na leitura e rejeitar pela API. |
| Elemento omitido e elemento presente vazio nas formas curta ou longa | Extrair nulo e vazio respectivamente. A API rejeita ambos se forem encaminhados à conferência. |

Nas rejeições, conferir `IllegalArgumentException` com a mensagem `NIS inválido.` e sem causa associada. Nos aceitos, a representação padrão é `[NIS oculto]` e a recuperação canônica é explícita. A sondagem comprova extração e conferência pós-extração. As fixtures são fragmentos sintéticos de campo e não certificam aceitação de um evento pelo portal.

## Execução e evidências

Na raiz do reator, selecione o JDK 8 em `JAVA_HOME` e execute o Wrapper. Repita em JDK 21, registrando a versão de JDK e as contagens de Surefire e Failsafe.

```powershell
.\mvnw.cmd -v
.\mvnw.cmd -B -ntp clean verify
python scripts/check-bytecode.py
```

Confira `NisTest` e `NisJarIT` nos dois módulos. Surefire cobre os 22 casos C1, o limite adicional de 12 dígitos, os caracteres imediatamente fora da faixa ASCII e os contratos de valor. Failsafe executa após o empacotamento na ordem do reator e deve confirmar as fixtures históricas e a origem em JAR. Verifique cobertura mínima de 90% de linhas e 85% de branches, JavaDoc com DocLint em ambos os JDKs, Dokka em JDK 21 e bytecode 52.

Para a compatibilidade, reconstrua em JDK 21, em diretório temporário separado, os artefatos do commit apontado por `v0.2.0`. Use a versão e o SHA-256 de japicmp fixados no [workflow Verify](../../.github/workflows/verify.yml). Passe ao script existente, nesta ordem, a raiz da baseline reconstruída, a raiz candidata, o JAR do japicmp e o repositório Maven local. Execute a comparação no mesmo JDK e registre o resultado de ambos os módulos. Espera-se que não haja incompatibilidades binárias ou de fonte.
