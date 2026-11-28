# Pesquisa técnica · Spec 010

**Data** 2026-11-27 · **Base** `18bfe9c` · **Revisão** [pareceres](../../docs/descoberta/010-contratos-documentos/revisao.md)

## D1 · Contratos segregados e padrões GoF

- **Decisão.** Três interfaces de valor (`Document`, `MaskedDocument`, `CheckDigitDocument`) e uma implementação esqueleto, `AbstractDocument`. A interpretação fica em `DocumentParser<D>`, a regra em `ValidationRule<T>` e a apresentação em `DocumentFormatter<D>`.
- **Padrões e o problema que resolvem:**
  - Strategy, para a variação de modo, algoritmo e apresentação.
  - Decorator, em `withRule`/`withPolicy`, para acrescentar regras sem herança.
  - Chain of Responsibility com Null Object, em `ValidationRule.and` e `accept`, para regras em sequência sem `null`.
  - Builder, em `DocumentParsers`, para montar documentos novos sem editar a biblioteca.
  - Métodos de fábrica estáticos, em `Cpf.parser(mode)`.
  - Command, em `DocumentCheck`, como item executável.
  - Facade, nos utilitários deprecados.
  - Adapter, no módulo Kotlin.
- **Motivo.** A constituição v0.5.0 pede contratos e extensão pelo consumidor. Os três tipos repetem estado e comportamento, e os utilitários estáticos impedem substituição. ISP: o NIS não tem DV por padrão nem máscara publicada, então implementa só `Document`.
- **Alternativas.** Uma interface única com todos os métodos forçaria o NIS a implementar DV e máscara (viola ISP). Herança entre documentos acoplaria regras distintas. O Visitor seria desnecessário, porque o resultado comum basta para listas heterogêneas.

## D2 · Resultado sem exceção, com motivo

- **Decisão.** `ValidationResult<D>` como classe abstrata com construtor package-private e subclasses finais `Valid` e `Invalid`, sem limite em `D`. `Rejection` traz `RejectionReason` e um código opcional.
- **Motivo.** Elimina `null` como sinal e exceção como fluxo de controle (diagnóstico 3, 6 e 7). Java 8 não tem tipos selados, e o construtor package-private impede subclasses externas sem gerar pontes sintéticas. Sem limite em `D`, `map(Cpf::applyMask)` produz `ValidationResult<String>`.
- **Alternativas.** `Optional<D>` perde o motivo. Uma exceção por motivo mantém o fluxo por exceção. Uma interface pública de resultado permitiria implementações que quebram o contrato.

## D3 · Compatibilidade de exceções e assinaturas

- **Decisão.**
  - `parseOrThrow` lança `IllegalArgumentException` exata, com a mensagem configurada no builder ("CPF inválido.", "CNPJ inválido.", "NIS inválido.").
  - `Cpf.parser(null)` e `Cnpj.parser(null)` lançam IAE, como as fábricas atuais. `DocumentCheck.cpf/cnpj` mantêm NPE.
  - Nenhuma sobrecarga nova nas fachadas.
  - `validatedCanonical` fica como ponte package-private até migrar os testes internos.
  - `DocumentCheck<T>` continua sem limite na classe.
- **Motivo.**
  - Cinco testes usam `assertThrowsExactly(IllegalArgumentException.class)`.
  - `NisValidator.isValid(null, null)` ficaria ambíguo com uma sobrecarga de modo.
  - Mudar o limite de `T` alteraria a assinatura genérica publicada na 007.

## D4 · Equivalência dos cálculos

- **Decisão.** `Modulus11` configurável pelo valor do caractere, pela sequência de pesos e pelo mapeamento do resto, com `compute` devolvendo `Optional<String>`. Os presets `cpf()` e `cnpj()` são validados contra `CpfCheckDigits` e `CnpjCheckDigits` em todos os vetores e em 10 000 bases geradas com semente fixa e congeladas como recurso de teste. Os cálculos antigos só são removidos depois dessa verificação. O bloqueio de 14 zeros (F-05) vira uma `ValidationRule<String>` nos modos com DV, e `Cnpj.hasValidCheckDigits()` o respeita.
- **Motivo.** O CPF atual usa pesos crescentes com `resto 10 → 0`, matematicamente equivalente à forma de pesos decrescentes com `resto < 2 → 0`. O CNPJ usa `ASCII − 48` com pesos cíclicos de 2 a 9. O `Optional` acomoda a fórmula da Caixa, que invalida o resto 1. A validação de domínio impede `%` negativo para caracteres abaixo de `'0'`.

## D5 · Máscara declarativa

- **Decisão.** `DocumentMask.of(pattern)` com `9` = `[0-9]`, `A` = `[A-Z]`, `X` = `[A-Z0-9]` e literais para o resto. `unmask` aceita a forma canônica (os mesmos slots, sem literais) ou a máscara completa exata. `apply` exige um canônico conforme os slots. CPF: `999.999.999-99`. CNPJ: `XX.XXX.XXX/XXXX-99`.
- **Motivo.** Substitui quatro expressões regulares e dois `applyMask` por `substring`, preservando o comportamento atual: máscara parcial rejeitada, minúsculas rejeitadas e DV do CNPJ numérico.

## D6 · NIS e proveniência

- **Decisão.**
  - `NisValidationMode { FORMAT_ONLY, CAIXA_FGTS_V016, LEGACY_MOD11 }`, com `FORMAT_ONLY` como padrão.
  - `Modulus11.pisCaixaFgtsV016()`: pesos `3,2,9,8,7,6,5,4,3,2`; resto 0 → 0; resto 1 → inválido; senão `11 − resto`.
  - `Modulus11.pisLegacyMod11()`: restos 0 e 1 → 0.
  - `NisRules.repeatedDigits()`: `ValidationRule<Nis>` que rejeita onze algarismos iguais, com fonte RV_03 das Regras de Validação v1.3 do SIPREV Gestão e alcance no SIPREV.
  - `LEGACY_MOD11` inclui a guarda. `CAIXA_FGTS_V016` não a inclui; o consumidor a encadeia por `withRule`.
- **Motivo.** Princípio V e decisão do usuário: nada é descrito como "oficial" sem norma confirmada. A fórmula da Caixa tem alcance documentado só para PIS/Pasep no FGTS ([006-nis](../../docs/descoberta/006-nis/recorte.md)). Os legados divergem no caso 17 ([C1](../../docs/descoberta/c1/nis-matriz.md)).
- **Vetores conferidos:**

  | Vetor | Soma | Resto | Caixa FGTS v016 | Legado |
  | --- | --- | --- | --- | --- |
  | 07 | 0 | 0 | aceita sem guarda | rejeita pela guarda |
  | 09 | 202 | 4 | rejeita (DV 7) | rejeita |
  | 15 | 2 | 2 | aceita | aceita |
  | 16 | 12 | 1 | rejeita | rejeita |
  | 17 | 12 | 1 | rejeita | aceita |
  | 18 | 10 | 10 | rejeita (DV 1) | rejeita |
  | 19 | 22 | 0 | aceita | aceita |
  | 20 | 22 | 0 | rejeita | rejeita |
  | 21 | 49 | 5 | aceita | aceita |

## D7 · `DocumentCheck` sobre interpretadores

- **Decisão.**
  - `of(input, DocumentParser<D>)` como fábrica genérica. `cpf`, `cnpj` e `nis` delegam a ela.
  - `evaluate()` chama `parse` uma vez, sem `try/catch`.
  - `RejectionReason.POLICY` vira `REJECTED_BY_POLICY`; os demais motivos viram `REJECTED_BY_VALIDATION`.
  - A política do item continua sendo substituída por `withPolicy` e roda depois do interpretador.
  - `DocumentCheckResult.getRejection()` é aditivo.
- **Motivo.** OCP: documentos do consumidor entram em listas sem mudanças na biblioteca. A semântica da 007 é preservada.

## D8 · Kotlin

- **Decisão.**
  - `String?.toCpfOrNull(mode)`, `toCnpjOrNull(mode)` e `toNisOrNull(mode)` devolvem as value classes.
  - `DocumentParser<D>.parseOrNull(String?)` serve qualquer interpretador.
  - `Cnpj.hasValidCheckDigits()` é acrescentado.
  - `Nis.kt` passa a delegar a `Nis.parser()` antes da deprecação das fachadas.
  - Não haverá extensão `fold`.
- **Motivo.** Os interpretadores genéricos devolvem tipos Java, e as value classes continuam sendo a API idiomática. Uma extensão `fold` competiria com o membro SAM homônimo.
