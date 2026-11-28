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
| RF-07 | Compor conferência nativa, política do consumidor e apresentação explícita para CPF, CNPJ e NIS em listas heterogêneas. | [Spec 007](../specs/007-composicao-identidade/spec.md). Testes de `DocumentCheck` com os desfechos aceito, rejeitado por política e rejeitado por validação ([F-12](fontes.md)). |
| RF-08 | Representar moedas, políticas de arredondamento explícitas e valores monetários imutáveis, com rateio sem perda de centavos e conversão cambial exata e reversível. | [Spec 008](../specs/008-nucleo-monetario/spec.md). Testes de arredondamento, `ratear`, `alocar` e `TaxaCambio` com vetores sintéticos ([F-13 e F-14](fontes.md)). |
| RF-09 | Oferecer exemplos executáveis em Java e Kotlin que acompanhem a API pública. | [Spec 009](../specs/009-exemplos-utilizacao/spec.md). Módulo `brasil4j-exemplos`, executado em `verify` e sem deploy. |
| RF-10 | Expor contratos públicos de documento (interpretação com motivo da rejeição, regras componíveis, máscara completa e de segurança, DV por algoritmo) para os documentos da biblioteca e do consumidor, com validadores de DV do NIS opcionais e nomeados pela fonte. | [Spec 010](../specs/010-contratos-documentos/spec.md). Testes de contrato, equivalência de `Modulus11` com os cálculos anteriores e consumo pelos JARs ([F-15 a F-18](fontes.md)). |
| RF-11 | Avaliar expressões financeiras com arredondamento monetário final único, aplicar percentuais de forma conciliável, calcular a base por dentro (*gross-up*) e apurar tabelas progressivas parametrizadas pelo consumidor. | [Spec 011](../specs/011-calculadora-percentual-faixas/spec.md). Invariantes algébricas e vetores sintéticos ([F-21](fontes.md)). A biblioteca não embarca tabelas legais. |
| RF-12 | Conferir a estrutura do grupo histórico da CTPS do S-2200 v2.5 com proveniência declarada, e identificar a CTPS Digital pelo CPF. | [Spec 014](../specs/014-ctps-historica/spec.md). Casos sintéticos do `TCtps` e testes de diagnóstico sem dados pessoais ([F-19 e F-20](fontes.md)). |
| RNF-01 | Reutilizar a regra Java em Kotlin, com compatibilidade de execução em Java 8. | Testes pelos JARs em JDK 8/21 e bytecode 52. |
| RNF-02 | Núcleo sem dependências de runtime e com API documentada. | Dependências do POM, JavaDoc e exemplos de consumo. |
| RNF-03 | Rastrear regras, vetores e políticas. | [Fontes das regras](fontes.md) e specs correspondentes. |

A adoção em serviços exige decisão sobre diferenças de apresentação e migração. Existência, titularidade e situação cadastral são verificadas no contexto do consumidor. Calendário e referências fiscais são incrementos futuros.
