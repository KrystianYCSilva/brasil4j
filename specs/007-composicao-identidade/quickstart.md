# Guia de verificação · Spec 007

Os exemplos exercitam a API de composição da Spec 007.

## Java · compor e percorrer a lista

```java
import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.composicao.DocumentCheck;
import io.github.brasil4j.identidade.composicao.DocumentCheckResult;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.nis.Nis;
import io.github.brasil4j.identidade.nis.NisValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
```

No código consumidor, associe a regra ao tipo conhecido no leiaute. Neste exemplo sintético, o importador só aceita NIS com prefixo `00`.

```java
String campo = "01234567898";
Predicate<Nis> regraDoImportador = nis -> nis.getCanonicalValue().startsWith("00");
boolean aceitoDiretamente = NisValidator.isValid(campo, regraDoImportador);

List<DocumentCheck<?>> itens = Arrays.asList(
    DocumentCheck.cpf(campo, CpfValidationMode.FORMAT_ONLY).withFormatter(Cpf::applyMask),
    DocumentCheck.nis(campo).withPolicy(regraDoImportador),
    DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.ALPHANUMERIC)
        .withFormatter(Cnpj::applyMask)
);

List<DocumentCheckResult> resultados = new ArrayList<>();
for (DocumentCheck<?> item : itens) {
    resultados.add(item.evaluate());
}
```

`aceitoDiretamente` é `false`. A lista produz `ACCEPTED`, `REJECTED_BY_POLICY` e `ACCEPTED`, nessa ordem. As duas apresentações produzidas são `012.345.678-98` e `12.ABC.345/01DE-35`. O segundo item tem saída ausente. Para recuperar a apresentação, o consumidor chama `getFormattedValue()` no resultado. Usar a mesma referência `regraDoImportador` na API direta e na lista demonstra a reutilização do contrato.

## Kotlin · SAM e saída opcional

```kotlin
import io.github.brasil4j.identidade.cnpj.Cnpj
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode
import io.github.brasil4j.identidade.composicao.DocumentCheck
import io.github.brasil4j.identidade.cpf.Cpf
import io.github.brasil4j.identidade.cpf.CpfValidationMode
import io.github.brasil4j.identidade.nis.Nis
import io.github.brasil4j.kotlin.identidade.isValidNis
import java.util.function.Predicate

val campo: String? = "01234567898"
val regraDoImportador = Predicate<Nis> { it.canonicalValue.startsWith("00") }
val aceitoDiretamente = campo.isValidNis(regraDoImportador)

val itens: List<DocumentCheck<*>> = listOf(
    DocumentCheck.cpf(campo, CpfValidationMode.FORMAT_ONLY).withFormatter(Cpf::applyMask),
    DocumentCheck.nis(campo).withPolicy(regraDoImportador),
    DocumentCheck.cnpj("12ABC34501DE36", CnpjValidationMode.FORMAT_ONLY)
        .withPolicy { it.root == "12ABC345" }
        .withFormatter(Cnpj::applyMask),
)
val resultados = itens.map { it.evaluate() }
val primeiraSaida: String? = resultados[0].formattedValue.orElse(null)
val saidaCnpj: String? = resultados[2].formattedValue.orElse(null)
```

A lista mantém os mesmos três estados do exemplo Java. O CNPJ em `FORMAT_ONLY` resulta em `12.ABC.345/01DE-36`, embora o DV divirja. A política e a formatação recebem o CNPJ já construído no modo escolhido. Para montar um item sem política ou apresentação, use a fábrica e omita a respectiva chamada de configuração.

## Uma política local de NIS · exemplo da UEM

No sistema da UEM, a expressão `nis -> ValidationUtils.isPis(nis.getCanonicalValue())` adapta o helper local a `Predicate<Nis>`. O consumidor pode fornecer esse predicado em `.withPolicy(...)` ou na sobrecarga do `NisValidator`. Os testes da biblioteca usam funções sintéticas para reproduzir estes desfechos e observar chamadas. Os casos 15 e 17 são aceitos e preservam zeros iniciais, o 09 é rejeitado pela política, e o 02 é rejeitado pela forma com zero chamadas aos callbacks. [Matriz C1](../../docs/descoberta/c1/nis-matriz.md)

## Verificação em JDK 8 e JDK 21

Na raiz do reator, rode primeiro em JDK 8 e depois em JDK 21. Selecione cada JDK em `JAVA_HOME`, confira a versão usada pelo Maven e execute os comandos. Guarde os relatórios da primeira rodada antes de repetir, pois `clean` remove os diretórios de saída. Os JARs da segunda rodada são os candidatos da comparação de APIs.

```powershell
.\mvnw.cmd -v
.\mvnw.cmd -B -ntp clean verify
python scripts/check-bytecode.py
```

Conferir `target/surefire-reports`, `target/failsafe-reports` e `target/site/jacoco` nos módulos. Os dois `DocumentCheckJarIT` devem executar, com origem em JAR para os tipos exigidos no [plano](plan.md#testes-e-rastreabilidade). Os dois JARs precisam ter bytecode 52. Conferir DocLint nas duas rodadas, Dokka no JDK 21, 90% ou mais de linhas e 85% ou mais de branches por módulo com branches. Registrar a versão do JDK, contagens de testes e resultados dos gates de cada rodada.

## Comparação de APIs em JDK 21

Com o candidato já empacotado em JDK 21, use a mesma instalação de Java para reconstruir as baselines. Guarde os artefatos de cada base em diretório próprio. Obtenha japicmp 0.26.1 conforme o [workflow Verify](../../.github/workflows/verify.yml), aponte `$japicmp` para o JAR e `$mavenRepo` para o repositório efetivamente usado pelos builds. Verifique o SHA-256 antes de rodar o script. Use um caminho de `JAVA_HOME` do JDK 21 também para o script.

```powershell
$candidate = (Get-Location).Path
$baselineRoot = Join-Path ([IO.Path]::GetTempPath()) ('brasil4j-007-' + [guid]::NewGuid())
$baselines = [ordered]@{
    'v020' = '8e94704689ac4a48b174a29ab264f0f5eba0164d'
    'spec006' = '5fd9dae6f4059b5e25528c5717d10c7c2f0aa922'
}
$expected = '4b4f0301861d97cd2a05af3f7fa9173cc1dd3831b1284b2b9e2f81c4f1c2f858'
if ((Get-FileHash -LiteralPath $japicmp -Algorithm SHA256).Hash -ne $expected) {
    throw 'SHA-256 inesperado para japicmp 0.26.1'
}
New-Item -ItemType Directory -Path $baselineRoot | Out-Null
foreach ($entry in $baselines.GetEnumerator()) {
    $baseline = Join-Path $baselineRoot $entry.Key
    $archive = "$baseline.zip"
    git archive --format=zip "--output=$archive" $entry.Value
    if ($LASTEXITCODE -ne 0) { throw "Falha ao arquivar $($entry.Key)" }
    Expand-Archive -LiteralPath $archive -DestinationPath $baseline -ErrorAction Stop
    Push-Location $baseline
    try {
        .\mvnw.cmd -B -ntp "-Dmaven.repo.local=$mavenRepo" -Dmaven.test.skip=true package
        if ($LASTEXITCODE -ne 0) { throw "Falha ao empacotar $($entry.Key)" }
    } finally { Pop-Location }
    python scripts/check-api-compatibility.py $baseline $candidate $japicmp $mavenRepo
    if ($LASTEXITCODE -ne 0) { throw "Falha na comparação com $($entry.Key)" }
}
```

Registrar o resultado dos dois artefatos em ambas as comparações. A expectativa é zero incompatibilidades binárias e de fonte. A comparação com `5fd9dae` também cobre `Nis`, seus suportes Java e as extensões Kotlin da Spec 006.
