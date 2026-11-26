# Contrato público · IBGE e UF

| API Java | Contrato |
| --- | --- |
| `CodigoMunicipio.parse(String)` | Exatamente sete dígitos ASCII. Lança `IllegalArgumentException` para nulo ou inválido, sem reproduzir a entrada. |
| `getCanonicalValue()`, `getUfCode()` | Retornam o valor original e o prefixo de dois caracteres. |
| `getUf()` | `Optional<Uf>`, vazio para prefixo desconhecido. |
| `matchesUf(Uf)` | Verdadeiro quando o prefixo corresponde ao código da UF. UF nula ou prefixo desconhecido retornam falso, inclusive quando ocorrem juntos. |
| `Uf.getIbgeCode()`, `Uf.fromIbgeCode(String)` | Código textual e busca exata. A busca retorna `Optional.empty()` para nulo, desconhecido ou apresentação alternativa. |
| `Uf.getNome()`, `getSigla()`, `getCapital()`, `getDdds()` | Nome oficial, sigla/acrônimo, capital e lista imutável de DDDs de dois dígitos em ordem crescente. |
| `Uf.fromName(String)` | `Optional<Uf>`, busca pelo nome completo sem distinção de caixa ou acentos e com `trim()` nas extremidades. Nulo, vazio, sigla, abreviação e nome desconhecido retornam vazio. |
| `equals`, `hashCode`, `toString` | Base canônica. O código municipal é público. |

Em Kotlin, `CodigoMunicipio(String)`, `String.toCodigoMunicipio()` e propriedades `canonicalValue`, `ufCode`, `uf: Uf?`. Também expõe `matchesUf(Uf?)` e `toJava()`. `matchesUf` delega ao núcleo, inclusive quando a UF é nula. O retorno de `override fun toString(): String` é o código canônico, também em interpolação e quando a instância está representada como `Any`. A igualdade e o hash valem entre objetos Kotlin. Para comparar com o objeto Java, use `toJava()`.

A API Kotlin usa o mesmo enum Java, com `Uf.PR.nome`, `sigla`, `capital` e `ddds`.

O construtor e a extensão Kotlin recebem `String` não nula e delegam ao parser Java. Texto inválido segue o mesmo contrato de `IllegalArgumentException`, com mensagem sem a entrada. O tratamento de nulos provenientes de tipos de plataforma cabe à borda Kotlin; `CodigoMunicipio.parse(null)` segue o contrato Java da tabela.
