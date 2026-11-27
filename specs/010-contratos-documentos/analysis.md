# Registro de execução · Spec 010

**Base** `18bfe9c` mais os commits de documentação da 010 · **Executor** Claude Code · **Início** 2026-11-27

## Ponto de partida (T001)

O `clean verify` em modo offline, no Corretto 21.0.13 com o Wrapper do reator, passou com 480 testes e zero falhas, o mesmo total registrado no fechamento da Spec 007. A rodada em Corretto 8u512 fica para o gate final (T051), para não disputar os diretórios `target/` durante as etapas.

## Rebase sobre a Spec 009 (2026-11-27)

A pedido do usuário, a 010 foi rebaseada sobre `009-exemplos-utilizacao` (`ad3f6e7`, que contém a 008 em `c0a91af`), sem conflitos. O estado anterior ficou em `backup/010-antes-rebase`. Depois do rebase, o `clean verify` no Corretto 21.0.13 passou com 554 testes: núcleo 392, Kotlin 134 e exemplos 28. O `brasil4j-exemplos` usa as fachadas de CPF, CNPJ e NIS e passa a ser gate de regressão do consumidor em cada etapa.

## Etapas

| Etapa | RED (falha esperada) | GREEN (mudança mínima) | Refatoração |
| --- | --- | --- | --- |
| 1 · Caracterização | Não se aplica: as regressões foram escritas sobre o comportamento atual e passaram verdes de imediato. | `CharacterizationTest` fixa o `toString` dos três tipos, as mensagens com IAE exata, o modo nulo, o bloqueio de 14 zeros e a rejeição de máscara parcial e minúsculas. | — |
| 2 · Resultado, rejeição e regra | `RejectionTest`, `ValidationResultTest` e `ValidationRuleTest` não compilavam: `cannot find symbol class Rejection`. | `RejectionReason`, `Rejection`, `ValidationResult` (construtor package-private, `Valid`/`Invalid` privados) e `ValidationRule` com `of`, `accept` e `and`. Os 3 testes passaram. | A mensagem padrão "Documento inválido." ficou numa constante interna reutilizada pelo parser. |
| 3 · Algoritmo de DV | `Modulus11Test` e os dois testes de equivalência não compilavam: `cannot find symbol class Modulus11`. | `CheckDigitAlgorithm` e `Modulus11` (builder, `cpf()`, `cnpj()`). A equivalência ao vivo conferiu 1 000 000 de combinações base+DV por documento contra `CpfCheckDigits` e `CnpjCheckDigits`, sem divergência. | Um gerador temporário congelou com os algoritmos antigos 10 000 bases por documento em `src/test/resources/verificacao/*-equivalencia.csv`, e os testes passaram a ler esses CSVs. Os modos e `Cpf.hasValidCheckDigits` usam `Modulus11`. O bloqueio de 14 zeros virou `CnpjValidationMode.hasCheckDigits`. `CpfCheckDigits` e `CnpjCheckDigits` foram removidos. Núcleo, Kotlin e exemplos verdes. Os presets PIS foram retirados desta etapa para entrar com RED próprio na etapa 7. |
| 4 · Máscara | `DocumentMaskTest` não compilava: `cannot find symbol class DocumentMask`. | `DocumentMask` com slots `9`, `A` e `X`, `unmask` (canônico ou máscara completa exata), `apply` e `applySecure` (prefixo e sufixo ocultos, literais preservados, caractere de ocultação não alfanumérico). | Os `canonicalize` de `CpfFormatter` e `CnpjFormatter` e os `applyMask` de `Cpf` e `Cnpj` delegam a `Cpf.MASK` (`999.999.999-99`) e `Cnpj.MASK` (`XX.XXX.XXX/XXXX-99`). As quatro expressões regulares e os `substring` saíram. Núcleo, Kotlin e exemplos verdes, sem alteração de asserções. |
| 5 · Contratos de valor | `AbstractDocumentTest` não compilava: `cannot find symbol class AbstractDocument`. | `Document`, `MaskedDocument`, `SecurityMaskedDocument` (default `applySecurityMask()` com `*`), `CheckDigitDocument` e `AbstractDocument` (`equals`/`hashCode` finais por classe e canônico; `toString` "[documento oculto]"). `Cpf` e `Cnpj` implementam as três capacidades; `Nis` só a máscara de segurança. Máscara de segurança: CPF 3+2, CNPJ 2+2, NIS 3+1 sem pontuação. `Cnpj.hasValidCheckDigits()` respeita F-05. | `getCanonicalValue` foi mantido como override fino nos três tipos, preservando o JavaDoc. `Nis.hasCanonicalFormat` usa a máscara canônica interna. Núcleo, Kotlin e exemplos verdes. **Gate japicmp pendente:** as baselines `v0.2.0` e `5fd9dae` foram empacotadas no scratchpad, mas o JAR do japicmp 0.26.1 não existe na máquina e o download aguarda autorização. |
