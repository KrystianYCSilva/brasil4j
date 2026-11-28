# Pareceres da descoberta · Spec 010

**Data** 2026-11-27 · **Base** `18bfe9c` · **Revisão** O Claude Code redigiu a proposta. O Antigravity a revisou via Agy em `plan` com sandbox, usando somente ferramentas de leitura de arquivo. O usuário decidiu os pontos de domínio.

## Parecer do Antigravity

**Veredito: aprova com ajustes.** A proposta atende ao objetivo de contratos, composição, polimorfismo e extensão pelo consumidor. O parecer conferiu o rascunho contra o código, os testes, as fontes e o script de compatibilidade e encontrou seis bloqueantes, seis pontos importantes e duas sugestões.

## Conferências e decisões

| Ponto | Verificação | Decisão |
| --- | --- | --- |
| Exceção de `parseOrThrow` | Cinco testes de NIS usam `assertThrowsExactly(IllegalArgumentException)`. Uma subclasse quebraria esses testes. | Manter `IllegalArgumentException` exata e as mensagens atuais. O motivo fica disponível por `parse`. Modo nulo continua gerando `IllegalArgumentException` nas fábricas de CPF e CNPJ. |
| Sobrecargas e pontes internas | `NisValidator.isValid(null, null)` ficaria ambíguo com uma nova sobrecarga de modo. Testes internos usam `validatedCanonical`. | Não acrescentar sobrecargas às fachadas. Manter a ponte package-private até migrar os testes. |
| Bloqueio de 14 zeros | `CnpjCheckDigits` rejeita `00000000000000` (F-05). Um módulo 11 puro aceitaria. | Bloqueio como regra explícita dos modos com DV e semântica definida para `hasValidCheckDigits`. `Modulus11` valida nulo, comprimento e caracteres. |
| DV do NIS | A fórmula da Caixa v016 já está registrada na descoberta da 006 e invalida o resto 1. Os legados aceitam o caso 17 com DV 0. As somas dos vetores C1 foram reconferidas. | **Decisão do usuário:** o NIS valida por padrão só tamanho e formato. Os validadores de DV ficam disponíveis como opção, nomeados pela fonte e pelo alcance. Nenhuma regra é chamada de oficial sem fonte normativa confirmada. `Nis` não implementa `CheckDigitDocument`. |
| Privacidade | Um `toString` padrão canônico vazaria documentos do consumidor. Um resultado que concatenasse o documento vazaria o CNPJ nos diagnósticos. | `AbstractDocument` oculta por padrão. `Cnpj` sobrescreve. `ValidationResult` mostra só o estado e o motivo. |
| Compatibilidade binária | O japicmp roda com `--include-synthetic` e falha em quebras binárias ou de fonte. | Rodar o script já na etapa dos contratos de valor, contra `v0.2.0`, `5fd9dae` e `18bfe9c`. Construtor package-private em `ValidationResult`. `DocumentCheck<T>` sem limite na classe. |
| `DocumentCheck` | `withPolicy` substitui na 007, mas encadearia no parser. | Preservar a substituição no item e documentar o contraste. Mapear `POLICY` para `REJECTED_BY_POLICY`. |
| Ordem TDD | O teste de equivalência vinha antes do algoritmo. A deprecação vinha antes da migração do Kotlin. | Equivalência na etapa do algoritmo, antes de remover os cálculos antigos. Migrar `Nis.kt` antes de deprecar. `@deprecated` no JavaDoc. |
| Kotlin | Parsers genéricos devolvem tipos Java. Uma extensão `fold` concorreria com o membro. | Extensões `toCpfOrNull`, `toCnpjOrNull` e `toNisOrNull` com value classes. Sem extensão `fold`. `Cnpj.kt` ganha `hasValidCheckDigits`. |
| Builder e máscara | Faltavam ligação direta com o algoritmo, um normalizador próprio e um símbolo só para letra. | Builder com `checkDigits`, `normalizer` e `errorMessage`. Máscara com `9` para dígito, `A` para letra e `X` para alfanumérico. |
| Pacotes compartilhados | A Spec 008 pode precisar de DV e máscara. | **Decisão do usuário:** `io.github.brasil4j.verificacao` e `io.github.brasil4j.mascara`. |
| Escopo e terminologia | `CodigoMunicipio` pertence a outro contexto. Alguns padrões estavam nomeados de forma imprecisa. | `CodigoMunicipio` fica fora da 010. Usar implementação esqueleto, método de fábrica estático e Command nos nomes. |

As decisões acima entram na especificação e no plano da 010. A guarda de repetição no modo da Caixa FGTS v016 fica para o clarify.
