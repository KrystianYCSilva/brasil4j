# Descoberta da Spec 010 · Contratos, polimorfismo e composição

**Data** 2026-11-27 · **Base** `18bfe9c` (integra a Spec 007) · **Estado** recorte revisado e decidido com o usuário, pronto para formalização. [Pareceres e decisões](revisao.md)

## Motivação

A Spec 007 pretendia oferecer contratos e composição para CPF, CNPJ e NIS. A [pesquisa da 007](../../../specs/007-composicao-identidade/research.md) rejeitou a superinterface nos valores (D3) e os contratos próprios (D1), e a entrega ficou em um item `DocumentCheck<T>` sobre `Predicate` e `Function` do JDK. O objetivo do projeto é outro: a biblioteca deve oferecer contratos públicos, reuso por composição e polimorfismo, com SOLID e padrões GoF. O consumidor deve conseguir criar seus próprios documentos e regras no mesmo padrão. A [constituição v0.5.0](../../../.specify/memory/constitution.md#engenharia-jvm) registra esse objetivo.

## Diagnóstico do núcleo em `18bfe9c`

| # | Problema | Evidência |
| --- | --- | --- |
| 1 | Valores sem contrato comum, com `canonical`, `getCanonicalValue`, `equals`, `hashCode` e `toString` repetidos | `Cpf`, `Cnpj`, `Nis` |
| 2 | Validação e apresentação em classes estáticas, que não podem ser injetadas nem substituídas | `CpfValidator`, `CnpjValidator`, `NisValidator`, `*Formatter` |
| 3 | Modos sinalizam rejeição com `null`. O auxiliar `requiring` existe só no CNPJ | `CpfValidationMode`, `CnpjValidationMode` |
| 4 | Módulo 11 em duas formulações independentes | `CpfCheckDigits`, `CnpjCheckDigits` |
| 5 | Máscara em quatro expressões regulares e dois `applyMask` por `substring` | `CpfFormatter`, `CnpjFormatter`, `Cpf`, `Cnpj` |
| 6 | Rejeição sem motivo, sem distinguir forma de DV | `parse` e `isValid` |
| 7 | Item de conferência fechado para novos documentos, com exceção como fluxo de controle | `DocumentCheck` |
| 8 | Consumidor sem meio de compor um documento novo com máscara, DV e regras da biblioteca | — |

## Proposta para formalização

1. **Contratos de valor.** `Document`, `MaskedDocument` e `CheckDigitDocument` segregam as capacidades. `AbstractDocument` concentra igualdade por classe e canônico, com representação textual oculta por padrão. CPF e CNPJ implementam os três contratos. NIS implementa só `Document`.
2. **Resultado e regra.** `ValidationResult<D>`, com `Valid` e `Invalid`, e `Rejection`, com motivo e código simbólico, substituem exceção e `null` como sinal. `ValidationRule<T>` compõe regras em cadeia, com Null Object.
3. **Interpretação.** `DocumentParser<D>` interpreta sem lançar exceção para entrada inválida e aceita decoração por regras e políticas. Um builder compõe máscara ou normalizador, algoritmo de DV, regras e mensagem segura. CPF, CNPJ e NIS são construídos por esse builder.
4. **Blocos compartilhados.** `io.github.brasil4j.verificacao` oferece `CheckDigitAlgorithm` e `Modulus11`, e `io.github.brasil4j.mascara` oferece `DocumentMask`, ambos reutilizáveis por outros contextos.
5. **NIS.** O padrão continua estrutural, com tamanho e formato. Os validadores de DV ficam disponíveis como opção explícita, nomeados pela fonte e pelo alcance: fórmula publicada no Caderno do Agente Financeiro v016 da Caixa, com alcance de PIS/Pasep no FGTS, e convenção técnica observada em Declarações e UEM. Nenhum dos dois é descrito como regra oficial do NIS em geral. [Alcance das fontes](../006-nis/recorte.md) · [Matriz C1](../c1/nis-matriz.md)
6. **Compatibilidade.** A API publicada é preservada. Os utilitários estáticos passam a fachadas deprecadas. `parseOrThrow` mantém `IllegalArgumentException` exata e as mensagens atuais. `DocumentCheck` passa a aceitar qualquer `DocumentParser`.

O plano detalha assinaturas, ordem das etapas e verificações. A questão aberta é se o modo da Caixa FGTS v016 aplica a guarda de algarismos repetidos, cuja fonte é o SIPREV RV_03, de outro alcance.
