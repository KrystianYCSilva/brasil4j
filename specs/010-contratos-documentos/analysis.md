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
