# Arquitetura do brasil4j

## Contexto

Identidade Brasileira representa CNPJ, CPF e NIS como objetos de valor imutáveis. A apresentação é validada antes de extrair o valor canônico. CNPJ e CPF oferecem modos de validação. NIS confere a estrutura do campo textual, com igualdade pelo texto canônico e saída padrão oculta. Extração de arquivos, cadastro e políticas de validade pertencem aos consumidores.

Geografia representa o código municipal do IBGE como valor estrutural e oferece a tabela de UFs. Os DDDs são dados de referência agregados. Cadastro e vigência municipal pertencem ao consumidor.

## Módulos e pacotes

| Módulo | Pacote | Responsabilidade |
| --- | --- | --- |
| Reator Maven `brasil4j` | — | Coordenadas, build e verificações de documentação e cobertura. |
| `brasil4j-core` | `io.github.brasil4j.identidade.cnpj` | Valor CNPJ, validação numérica, alfanumérica ou de formato, e apresentação. |
| `brasil4j-core` | `io.github.brasil4j.identidade.cpf` | Valor CPF, política de formato ou DV numérico, e apresentação. |
| `brasil4j-core` | `io.github.brasil4j.identidade.nis` | Inscrição estrutural de NIS, PIS, Pasep ou NIT, com consulta de forma e composição explícita com regras e apresentações do consumidor. |
| `brasil4j-core` | `io.github.brasil4j.geografia` | Código municipal, comparação com UF e metadados das unidades federativas. |
| `brasil4j-kotlin` | `io.github.brasil4j.kotlin.identidade` | Value classes e extensões de documentos delegadas ao núcleo. |
| `brasil4j-kotlin` | `io.github.brasil4j.kotlin.geografia` | Value class e extensão de código municipal sobre o núcleo. |

O núcleo depende apenas do JDK em produção. Kotlin depende dele e da biblioteca padrão Kotlin. O consumidor escolhe o artefato conforme a linguagem.

## Plataforma e entrega

Maven Wrapper, JDK 8 e 21, JaCoCo por módulo e JavaDoc com DocLint. CI confere o bytecode 52 dos JARs. Requisitos precedem código pelo Spec Kit, e os testes precedem cada regra de domínio por TDD.
