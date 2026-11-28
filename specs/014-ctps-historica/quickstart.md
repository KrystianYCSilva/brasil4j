# Guia de verificação · Spec 014

Os cenários deste guia são exercitados no reator Maven. O exemplo Java parte dos campos que o consumidor já extraiu.

```java
import io.github.brasil4j.trabalhista.ctps.CtpsCheckResult;
import io.github.brasil4j.trabalhista.ctps.CtpsGroup;
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin;
import io.github.brasil4j.trabalhista.ctps.CtpsProfile;
import io.github.brasil4j.trabalhista.ctps.CtpsRecord;

CtpsGroup group = CtpsGroup.of("0000123", "00004", "PR", CtpsOrigin.PHYSICAL, false);
CtpsCheckResult result = CtpsProfile.ESOCIAL_S2200_V2_5.check(group);
CtpsRecord record = result.getRecord().get(); // cenário com desfecho CONFORMING
```

Em Kotlin, a entrada anulável distingue grupo ausente de grupo fornecido. A projeção legada conforme fornece o registro e deixa a UF de expedição ausente. [Contrato e assinaturas](contracts/api.md)

```kotlin
import io.github.brasil4j.kotlin.identidade.checkCtps
import io.github.brasil4j.kotlin.identidade.issuingUfOrNull
import io.github.brasil4j.kotlin.identidade.recordOrNull
import io.github.brasil4j.trabalhista.ctps.CtpsGroup
import io.github.brasil4j.trabalhista.ctps.CtpsOrigin
import io.github.brasil4j.trabalhista.ctps.CtpsProfile

val group: CtpsGroup? = CtpsGroup.of("0123456", "7890", "PR", CtpsOrigin.CPF_PROJECTION, false)
val result = group.checkCtps(CtpsProfile.ESOCIAL_S2200_V2_5)
val record = requireNotNull(result.recordOrNull)
check(result.issuingUfOrNull == null)
```

## Fragmentos compartilhados do S-2200 v2.5

Os arquivos XML UTF-8 ficam em `brasil4j-core/src/test/resources/ctps/esocial-2.5/`, com nomes por ID e um catálogo dos desfechos e campos afetados. O Kotlin consome os mesmos recursos pelo `testResources` existente. Cada fixture tem o namespace `http://www.esocial.gov.br/schema/evt/evtAdmissao/v02_05_00` e representa o caminho `eSocial/evtAdmissao/trabalhador/documentos/CTPS`. São projeções sintéticas para conferir somente a extração e o tipo `TCtps`.

| ID | Número | Série | UF | Desfecho | Campo afetado |
| --- | --- | --- | --- | --- | --- |
| 01 | *grupo omitido* | — | — | `ABSENT` | — |
| 02 | `0000123` | `00004` | `PR` | `CONFORMING` | — |
| 03 | `A0` | `X` | `PR` | `CONFORMING` | — |
| 04 | `A-0` | `1A` | `PR` | `CONFORMING` | — |
| 05 | Dois espaços U+0020 | Um espaço U+0020 | `PR` | `CONFORMING` | — |
| 06 | `0000123` | *elemento omitido* | `PR` | `INCOMPLETE` | `SERIES` |
| 07 | `1` | `4` | `PR` | `OUT_OF_PROFILE` | `NUMBER` |
| 08 | `123456789012` | `4` | `PR` | `OUT_OF_PROFILE` | `NUMBER` |
| 09 | `123` | `123456` | `PR` | `OUT_OF_PROFILE` | `SERIES` |
| 10 | `123` | `4` | `XX` | `OUT_OF_PROFILE` | `UF` |
| 11 | `123` | `4` | `pr` | `OUT_OF_PROFILE` | `UF` |

Para um grupo omitido, o extrator passa `null`. Para um grupo presente, o teste declara a origem e a indicação de perda separadamente. Usar `UNDETERMINED` e `false` nos vetores principais. Selecionar filhos diretos com namespace, sem trim ou conversão numérica. Elemento ausente vira `null` e elemento presente sem conteúdo vira `""`. Desabilitar DTD, entidades externas e acesso externo no parser de teste. Preservar o texto entregue após a decodificação normal do XML.

A contraprova offline pode isolar o `TCtps` original em um elemento pai mínimo que permita a omissão do grupo. A presença, ordem e multiplicidade de nós são verificações do XML. Os 11 casos principais produzem 5 fragmentos conformes ao XSD, incluindo o omitido, e 6 não conformes. A API refina o desfecho do ausente e do incompleto. Usar o XSD original identificado em [D1](research.md#d1--autoridade-e-limites-do-perfil-histórico) para conferência pontual, com a execução regular apoiada nos vetores locais.

## Cenários complementares

- **Estado e facetas** — Cobrir `documentos` ausente e `<documentos/>` como grupo ausente, `<CTPS/>` com os três campos faltantes, `<nrCtps/>` com série e UF válidas, e precedência de um campo omitido mesmo quando outro é inválido. Com todos os campos fornecidos, agregar as violações na ordem definida no contrato. Testar número e série nos limites inclusivos, todas as 27 UFs e siglas que diferem por espaços ou caixa.
- **Unicode** — Usar `U+1F600` sozinho como número curto, e `A` seguido de `U+1F600` como mínimo aceito. Aceitar dez ou quatro letras mais `U+1F600` nos máximos respectivos de número e série. Preservar `A` seguido de `U+0301` como dois pontos de código sem composição. Para esses casos usar expectativas normativas, pois o JAXP embutido dos dois JDKs divergiu na sondagem. [Decisão D4](research.md#d4--comprimento-unicode-e-verificação)
- **Proveniência** — Passar `0123456`, `7890` e `PR` com as três origens. O registro deve ser recuperável nos três casos, com UF de expedição só no físico. Passar `123` e `4` com perda declarada e conferir a recuperação sem zeros acrescentados. Repetir a conferência com grupo incompleto ou fora do perfil para comprovar a preservação dos metadados.
- **Privacidade** — Verificar `toString()` do grupo, registro e resultado, mensagens geradas pela biblioteca e interpolação em Kotlin. Usar valores sintéticos distintivos. O diagnóstico pode mencionar o nome do campo `UF` e deve omitir seu conteúdo.
- **CTPS Digital** — Pelo contrato atual de CPF nos JARs, verificar `28001238938` nos dois modos e `28001238939` aceito em `FORMAT_ONLY` e rejeitado em `NUMERIC`. Kotlin testa `toCpfOrNull(mode)`. A origem `CPF_PROJECTION` permanece um metadado do grupo legado.

## Execução local

1. Rodar `.\mvnw.cmd -B -ntp clean verify` com `JAVA_HOME` apontando separadamente para JDK 8 e JDK 21. Os testes de unidade cobrem os estados, facetas e metadados. Os dois `CtpsJarIT` do Failsafe leem a matriz compartilhada, verificam os quatro desfechos, campos afetados, preservação, proveniência e usam `codeSource` para confirmar as classes do core e as extensões Kotlin a partir dos JARs. Aferir JaCoCo, DocLint e Dokka conforme o plano.
2. Executar `python scripts/check-bytecode.py` após cada build. Deve confirmar bytecode 52 em ambos os JARs.
3. Reconstruir `v0.2.0` e `a95efda` em diretórios separados. Rodar `python scripts/check-api-compatibility.py <baseline> <candidato> <japicmp.jar> <maven-repo>` para cada base. Esperar compatibilidade binária e de fonte dos artefatos Java e Kotlin.
