# Registro de execução · Spec 010

**Base** `18bfe9c` mais os commits de documentação da 010 · **Executor** Claude Code · **Início** 2026-11-27

## Ponto de partida (T001)

O `clean verify` em modo offline, no Corretto 21.0.13 com o Wrapper do reator, passou com 480 testes e zero falhas, o mesmo total registrado no fechamento da Spec 007. A rodada em Corretto 8u512 fica para o gate final (T051), para não disputar os diretórios `target/` durante as etapas.

## Etapas

| Etapa | RED (falha esperada) | GREEN (mudança mínima) | Refatoração |
| --- | --- | --- | --- |
| 1 · Caracterização | Não se aplica: as regressões foram escritas sobre o comportamento atual e passaram verdes de imediato. | `CharacterizationTest` fixa o `toString` dos três tipos, as mensagens com IAE exata, o modo nulo, o bloqueio de 14 zeros e a rejeição de máscara parcial e minúsculas. | — |
