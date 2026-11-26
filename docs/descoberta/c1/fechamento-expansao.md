# C1 · Consolidação da revisão da Spec 005

**Data** 2026-11-26 · **Estado** C1 fechado com aceite de Krystian em 2026-11-26, registrado na T001.

## Base e alcance dos pareceres

- O pacote original tem manifesto `0E1D701E8FE30AF0443C787DEB20915D8991042A5FC4933BAA6A9E6E873A2C45` e 15 entradas verificadas. O Claude revisou o checkout, onde o manifesto estava indisponível. Os achados aproveitados foram confrontados com os artefatos e as fontes.
- O Antigravity concluiu a revisão técnica da r2 com **Aprovado com ressalvas**. O manifesto r2 `41C38D815588D4C1E36C68F26EE87F969941C1E96D5C0F4995510D3A66943E2B` identificou 17 arquivos íntegros. Conferi as 16 entradas também presentes no checkout antes dos ajustes. A outra entrada é o README do pacote. A r2 conserva os arquivos que o Antigravity avaliou.

## Decisões aplicadas

| Origem | Ação |
| --- | --- |
| Claude · contrato e escopo | Fixar os 27 pares de UF, os quatro vetores dos consumidores e o limite de adaptação a `Long`. Definir a v0.2.0 com japicmp, justificar Geografia, manter `toJava()` e delegar a seleção da spec à T001 e a documentação da entrega à T008. |
| Claude · consumidores | Registrar Core com Java 21 e Censo com Java 11, e separar a oferta estrutural da decisão de adoção por mantenedor. |
| Antigravity · M-01 e M-02 | Exigir texto canônico em Java e Kotlin, inclusive sob `Any` em Kotlin. Exigir comparação falsa de `0012345` com UF nula em ambos os módulos. |
| Antigravity · B-01 e B-02 | Explicitar rejeição de entradas inválidas sem vazamento do texto na mensagem. Usar o vetor sintético `3599999` para demonstrar resolução de prefixo sem DV ou consulta. Registrar que a comparação entre os tipos Java e Kotlin passa por `toJava()`. |
| Antigravity · B-03 | Acrescentar quatro casos reproduzidos à [matriz NIS](nis-matriz.md). Os novos casos cobrem resto zero, DV divergente, base repetida e máscara alternativa. |

O DV do código municipal, os cenários fiscais e a configuração HTTP dos consumidores ficam fora do aceite da API estrutural. A assinatura atual do Censo permanece como referência da sondagem. A checagem de integridade é sobre a r2 e o registro acima descreve os ajustes posteriores.

## Evidência e decisão de fechamento

Rodei nove vetores de conversão em Java 8 e 21 e reproduzi quatro casos adicionais de NIS com os validadores de Declarações e UEM em Java 21. As sondagens Kotlin reportadas pelo Antigravity caracterizam o desenho. A implementação posterior confirmou os contratos pelos JARs em Java 8 e 21. As bordas HTTP seguem com cada consumidor. [Evidência da Spec 005](../../../specs/005-municipio-ibge-uf/analysis.md#verificação-da-entrega--2026-11-26).

Krystian aprovou a [Spec 005](../../../specs/005-municipio-ibge-uf/spec.md) como oferta estrutural para a v0.2.0, com adoção decidida por cada consumidor. A revisão e a seleção da 005 para implementação constam na T001. NIS permanece em descoberta, com fonte oficial de DV e chamador de produção ainda em aberto.
