# Análise de consistência · 2026-11-26

| Requisito | Tarefa | Evidência |
| --- | --- | --- |
| FR-001 e SC-001 | T002, T003, T006 | Quatro vetores conferidos no IBGE. Gramática ASCII, texto e prefixo nos testes unitários e de contrato. |
| FR-002 e SC-003 | T002, T004 | Snapshot de 27 UFs, sem prefixos duplicados. Busca e conferência explícitas. |
| FR-003 e SC-002 | T003, T004, T006 | Erro de formato, prefixo desconhecido com UF nula, prefixo conhecido com UF divergente e vetor sintético sem consulta ao catálogo. Igualdade, hash e texto canônico. |
| FR-004 e SC-002 | T005 a T007 | Paridade Java/Kotlin para texto, formato, nulo na comparação e conversão. Contratos pelos JARs, dois JDKs, documentação, cobertura e japicmp da v0.2.0. |
| FR-005 e SC-004 | T009, T006, T007 | Dados oficiais, busca por nome, DDD compartilhado/imutável e consumo pelos JARs. |
| Governança e DDD | T001, T008 | Revisão aprovada e seleção da spec antes do código. Propagação do novo contexto com a entrega. |

O [fechamento do C1](../../docs/descoberta/c1/fechamento-expansao.md) registra a base revisada, as decisões e as pendências. A reprodução Java caracterizou os vetores e a transformação para `Long`. Os dois pareceres foram consolidados. O aceite de Krystian foi registrado na T001.

## Verificação da entrega · 2026-11-26

- T003–T005 e T009 seguiram TDD. Os ciclos falharam inicialmente por falta da API ou por divergência de igualdade ou texto canônico. Os testes passaram após a implementação. Nome/capital e DDD tiveram ciclos próprios. O teste de referência do enum cobre as 27 UFs.
- T006 e T007 passaram com Maven Wrapper `clean verify` em Corretto 8u512 e 21.0.13. Por JDK, 343 testes passaram sem falhas ou skips, com 204 unitários e 62 pelos JARs no núcleo, e 15 unitários e 62 pelos JARs no Kotlin. As sondagens de conversão numérica exercitam o adaptador em isolamento.
- JaCoCo atingiu 100% de linhas nos dois módulos e 84/84 branches no núcleo, nos dois JDKs. O Kotlin não apresentou branches contabilizados. JavaDoc com DocLint passou nos dois JDKs, e Dokka passou no JDK 21. O bytecode de todos os JARs é 52.
- japicmp 0.26.1 conferiu compatibilidade binária e de fonte para cada módulo produzido nos dois JDKs, incluindo membros sintéticos. A baseline veio dos JARs da v0.1.0 no ZIP Central local, verificados contra os SHA-256 do bundle. Só houve adições de API.

## Revisão de TL · 2026-11-26

- O Claude aprovou a implementação em `3158043`. O commit posterior `5454290` está sincronizado com `origin/main` e passou em JDK 8 e 21 antes do envio. A observação sobre o atraso do remoto descrevia a base disponível durante a revisão.
- T010 conecta a comparação ao job JDK 21 do CI. A referência é reconstruída a partir do commit `8a6780d` apontado pela tag v0.1.0. A execução local passou nos dois JARs produzidos por cada JDK. O ensaio rejeitou uma remoção pública no núcleo e outra no Kotlin. Uma mudança sintética de exceção verificada confirmou a falha apenas na comparação de fonte.
- T011 documenta as marcas combinantes removidas e as apresentações que ficam fora da normalização. O `clean verify` passou com 345 testes por JDK e bytecode 52. O Claude aprovou T010 e T011 após revalidação independente. O parecer final do Antigravity aprovou os oito arquivos desta revisão para commit e push. A confirmação da release depende do workflow hospedado.
