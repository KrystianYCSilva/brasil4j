# Requisitos do brasil4j

Java 8 é a plataforma mínima. O primeiro caso investigado foi o cadastro de empresas do Obrigações Core. A descoberta e a comparação de alternativas estão no [C1](descoberta/c1/README.md).

| ID | Requisito | Evidência |
| --- | --- | --- |
| RF-01 | Interpretar CNPJ numérico e alfanumérico em forma canônica ou máscara completa. | Vetores oficiais e casos de fronteira em Java e Kotlin. |
| RF-02 | Oferecer modos CNPJ alfanumérico padrão, numérico e só formato. | CNPJ numérico válido aceito no padrão, letras rejeitadas no modo numérico e DV divergente aceito só no modo de formato. |
| RF-03 | Interpretar CPF por formato e conferir o DV por opção explícita. | O mesmo CPF de formato válido e DV divergente passa por padrão e falha no modo numérico. |
| RF-04 | Expor valor canônico, partes e máscara com zeros preservados. | Igualdade por valor e apresentação de documentos testadas. O CPF é ocultado na representação textual padrão. |
| RF-05 | Interpretar o código municipal de sete dígitos, resolver o prefixo de UF e consultar nome, sigla, capital e DDDs da UF. | [Spec 005](../specs/005-municipio-ibge-uf/spec.md). Testes estruturais e dados oficiais das 27 UFs. |
| RF-06 | Conferir NIS, PIS, Pasep ou NIT estrutural em texto canônico e permitir que o consumidor forneça validação adicional e apresentação explícitas. | [Spec 006](../specs/006-nis-estrutural/spec.md). Vetores do C1, fragmentos de S-2200, testes de callbacks e proteção na representação padrão. |
| RNF-01 | Reutilizar a regra Java em Kotlin, com compatibilidade de execução em Java 8. | Testes pelos JARs em JDK 8/21 e bytecode 52. |
| RNF-02 | Núcleo sem dependências de runtime e com API documentada. | Dependências do POM, JavaDoc e exemplos de consumo. |
| RNF-03 | Rastrear regras, vetores e políticas. | [Fontes das regras](fontes.md) e specs correspondentes. |

A adoção em serviços exige decisão sobre diferenças de apresentação e migração. Existência, titularidade e situação cadastral são verificadas no contexto do consumidor. Calendário e referências fiscais são incrementos futuros.
