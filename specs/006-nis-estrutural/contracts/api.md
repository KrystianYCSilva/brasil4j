# Contrato público · NIS estrutural

## Java · `brasil4j-core`

Tipo público final `io.github.brasil4j.identidade.nis.Nis` com construção pela fábrica.

| Assinatura | Contrato |
| --- | --- |
| `public static Nis parse(String input)` | Aceita exatamente 11 caracteres ASCII de `0` a `9`. Preserva a entrada inteira. Falha de apresentação, inclusive nulo, lança `IllegalArgumentException` com mensagem `NIS inválido.` e sem causa associada. |
| `public String getCanonicalValue()` | Recupera explicitamente todos os 11 dígitos. |
| `public boolean equals(Object other)` | Igualdade por texto canônico entre valores `Nis` Java. Nulo, string e outro tipo são distintos. |
| `public int hashCode()` | Hash do texto canônico, consistente com a igualdade. |
| `public String toString()` | Retorna o marcador fixo `[NIS oculto]`. |

## Kotlin · `brasil4j-kotlin`

`@JvmInline value class Nis` em `io.github.brasil4j.kotlin.identidade`, com construtor primário privado que recebe e guarda o delegado Java. A entrada pública do construtor Kotlin é o texto.

| Assinatura | Contrato |
| --- | --- |
| `constructor(input: String?)` | Encaminha a entrada à fábrica Java, inclusive nulo. Usa a mesma falha pública. |
| `fun String.toNis(): Nis` | Conveniência para o texto não nulo. Usa a construção padrão. |
| `val canonicalValue: String` | Recupera explicitamente os 11 dígitos. |
| `fun toJava(): io.github.brasil4j.identidade.nis.Nis` | Devolve o mesmo valor Java imutável. Comparações com Java passam por essa conversão explícita. |
| `override fun toString(): String` | Delega ao núcleo. O marcador é o mesmo na chamada direta, na interpolação e quando o valor é tratado como `Any`. |

A igualdade e o hash gerados pela value class usam o delegado e preservam a igualdade por texto entre valores Kotlin, inclusive sob `Any`.

As extensões de topo ficam em `Nis.kt`, na classe JVM gerada `io.github.brasil4j.kotlin.identidade.NisKt`. O contrato dessas extensões é Kotlin. Chamadas Java usam a API do núcleo.

## Validação e formatação explícitas

As classes de suporte Java ficam no mesmo pacote de `Nis`. As funções Kotlin usam a mesma regra Java.

| API | Contrato |
| --- | --- |
| Java `NisValidator.isValid(String input)` · Kotlin `String?.isValidNis()` | Confere a mesma gramática da fábrica. Devolve `false` para entrada inválida, inclusive nulo. |
| Java `NisValidator.isValid(String input, Predicate<? super Nis> policy)` · Kotlin `String?.isValidNis(policy: Predicate<in JavaNis>)` | Executa a política uma vez sobre um NIS Java estruturalmente válido. Devolve `false` para forma inválida sem consultar a política. Devolve o resultado da política nos demais casos. Política nula é erro de uso e lança `NullPointerException`. |
| Java `NisFormatter.format(Nis nis, Function<? super Nis, String> formatter)` · Kotlin `Nis.format(formatter: Function<in JavaNis, String>)` | Aplica a função explicitamente ao valor conferido. Valor, função e resultado precisam ser não nulos. Argumento ou resultado nulo lança `NullPointerException`. O resultado é definido pelo consumidor e pode expor a inscrição. |

`JavaNis` designa `io.github.brasil4j.identidade.nis.Nis` no contrato Kotlin. Exceções lançadas pelas funções do consumidor propagam-se ao chamador. A biblioteca não registra o conteúdo nem muda o marcador de `toString()`. A política de ausência e a proteção das saídas e falhas produzidas pelo callback pertencem a quem o fornece.

## Limites e compatibilidade

Espaços, sinais, separadores, máscaras, letras, dígitos Unicode e comprimento divergente geram a mesma falha. Repetições e DV divergente são aceitos quando a forma é canônica. A extração de arquivo, a política de ausência, o DV, o cadastro e a identificação de emissor pertencem ao fluxo consumidor.

A adição é pública nos dois artefatos. O contrato de migração para adaptadores históricos é entregar o campo textual original. A [matriz de impacto](../../../docs/descoberta/006-nis/revisao.md#impacto-de-migração) registra as diferenças observadas para integrações que hoje removem pontuação ou verificam DV. Um fluxo com essas regras precisa preservá-las na sua própria etapa.
