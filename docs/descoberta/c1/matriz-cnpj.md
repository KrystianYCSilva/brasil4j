# C1 · matriz de CNPJ

**Execução em 25/11/2026.** A coluna Core mostra o valor devolvido por `normalizar` diretamente, em classes locais pré-compiladas e executadas no Corretto 21.0.13. A leitura de `Cnpj.kt` e dos testes versionados em `6e31e39` corrobora os resultados. A referência RFB e o JAR pronto foram executados em Corretto 8u512 e 21.0.13, com resultados iguais nas duas JVMs. A coluna Spec 001 indica o aceite proposto, a verificar em TDD.

| ID | Entrada | Proveniência | Java RFB | JAR 1.1.0 | Core | Spec 001 |
| --- | --- | --- | --- | --- | --- | --- |
| 01 | `12ABC34501DE35` | Manual / TesterCalculo e TesterValidacao | Aceita | `12ABC34501DE35` | `12ABC34501DE35` | Aceita |
| 02 | `12.ABC.345/01DE-35` | Manual | Aceita | `12ABC34501DE35` | `12ABC34501DE35` | Aceita |
| 03 | `1345C3A5000106` | TesterCalculo e TesterValidacao | Aceita | `1345C3A5000106` | `1345C3A5000106` | Aceita |
| 04 | `R55231B3000757` | TesterCalculo | Aceita | `R55231B3000757` | `R55231B3000757` | Aceita |
| 05 | `90.021.382/0001-22` | TesterCalculo + máscara / TesterValidacao | Aceita | `90021382000122` | `90021382000122` | Aceita |
| 06 | `90024778000123` | TesterCalculo, forma canônica | Aceita | `90024778000123` | `90024778000123` | Aceita |
| 07 | `90025108000121` | TesterCalculo, forma canônica | Aceita | `90025108000121` | `90025108000121` | Aceita |
| 08 | `90025255000100` | TesterCalculo, DV 00 | Aceita | `90025255000100` | `90025255000100` | Aceita |
| 09 | `90024420000109` | TesterCalculo, DV 09 | Aceita | `90024420000109` | `90024420000109` | Aceita |
| 10 | `90.024.778/000123` | TesterValidacao, aceito no exemplo | Aceita | `90024778000123` | `90024778000123` | Rejeita |
| 11 | `1.2ABC34501DE35` | Mutação sintética do manual | Aceita | `12ABC34501DE35` | `12ABC34501DE35` | Rejeita |
| 12 | `00000000000191` | Caso de zeros verificado pelo exemplo RFB | Aceita | `00000000000191` | `00000000000191` | Aceita |
| 13 | `12ABC34501DE36` | Mutação sintética do DV | Rejeita | Rejeita | Rejeita | Rejeita |
| 14 | `12ABC34501DEA5` | Mutação sintética do DV com letra | Rejeita | Rejeita | Rejeita | Rejeita |
| 15 | `00000000000000` | Sequência zerada | Rejeita | Rejeita | Rejeita | Rejeita |
| 16 | `12!ABC34501DE35` | Caracterização do Core | Rejeita | `12ABC34501DE35` | Rejeita | Rejeita |
| 17 | `␠12.ABC.345/01DE-35␠` | Espaços externos sintéticos | Aceita | `12ABC34501DE35` | `12ABC34501DE35` | Rejeita |
| 18 | `12.abc.345/01de-35` | Caixa baixa sintética | Rejeita | `12ABC34501DE35` | `12ABC34501DE35` | Rejeita |
| 19 | vazio | Vazio | Rejeita | Rejeita | Rejeita | Rejeita |
| 20 | `null` | Nulo | Rejeita | Rejeita | Rejeita (nulo) | Rejeita |
| 21 | `１２ABC34501DE35` | Dígitos de largura cheia | Rejeita | Rejeita | Rejeita | Rejeita |
| 22 | `R55231B3000700` | TesterValidacao, DV errado | Rejeita | Rejeita | Rejeita | Rejeita |
| 23 | `90.025.255/0001` | TesterValidacao, faltam os DVs | Rejeita | Rejeita | Rejeita | Rejeita |
| 24 | `90.024.420/0001A2` | TesterValidacao, letra no DV | Rejeita | Rejeita | Rejeita | Rejeita |
| 25 | `12.ABC.345/01DE-36` | Mutação sintética do DV formatado | Rejeita | Rejeita | Rejeita | Rejeita |
| 26 | `1345c3A5000106` | TesterValidacao, caixa baixa | Rejeita | `1345C3A5000106` | `1345C3A5000106` | Rejeita |
| 27 | `90.025.108/000101` | TesterValidacao, DV errado | Rejeita | Rejeita | Rejeita | Rejeita |

## Contrato observado

No Core, `EmpresasController.criar` recebe `@Valid CriarEmpresa`. `ValidadorCnpj` aplica `Cnpj.valido` ao campo, e `EmpresasService.criarEmpresa` constrói `Empresa`. A entidade mantém o texto canônico. `FundamentosRestTest` especifica 201 para máscara completa, 400 para DV errado e 409 para repetir o mesmo CNPJ canônico. `FundamentosIdentidadeTest` aceita caixa baixa, espaços externos e máscara parcial no domínio. O teste REST foi inspecionado em leitura. O campo nulo é rejeitado pela anotação se chegar ao validador, e a sondagem direta de `normalizar` lança `NullPointerException`.

A máscara parcial é o caso 6 do **TesterValidacao** da RFB, com resultado verdadeiro. O **TesterCalculo** lista a base `90.024.778/0001` e DV `23` em campos separados. A pontuação deslocada é uma mutação sintética que o exemplo Java da RFB aceita porque remove os separadores sem validar suas posições. O exemplo também usa trim e rejeita o valor zerado. A proposta de aceitar só a máscara completa ou a forma canônica é uma regra da API. [Manual e ZIP oficial](https://www.gov.br/receitafederal/pt-br/centrais-de-conteudo/publicacoes/documentos-tecnicos/cnpj).

## Alternativas e reprodução

- **Correção local.** Manter o cálculo e validar apresentação antes da normalização, se o cadastro decidir mudar seu contrato. Menor superfície na hipótese de um único consumidor. Validar a borda HTTP e observar clientes legados antes de aplicar.
- **Artefato publicado.** `io.github.johnpitter:cnpj-alfanumerico:1.1.0`, Apache-2.0. Exercitados `CNPJ.isValid` e `CNPJ.unformat`. A entrada com `!` é aceita, além de máscara parcial, caixa baixa e pontuação deslocada. Em sondagem separada, um pré-filtro para as duas apresentações estritas, seguido de `isValid` e `unformat`, reproduziu os 27 aceites e valores canônicos esperados em Java 8 e 21. O POM embutido confirma Apache-2.0 e lista apenas JUnit em escopo de teste. Permanecem em avaliação a API pública, as mensagens de erro e o esforço de manutenção. [Fonte e licença](https://github.com/JohnPitter/cnpj-alfanumerico).
- **Implementação própria.** Contrato estrito e único para consumidores Java e Kotlin. Código independente, sob Apache-2.0. Custo adicional de responsabilidade sobre regras, testes e versões. O contrato proposto vem da spec, e a decisão depende de benefício compartilhado.

Para reproduzir, usar o [ZIP oficial](https://www.gov.br/receitafederal/pt-br/centrais-de-conteudo/publicacoes/documentos-tecnicos/cnpj/codigos-cnpj.zip/view) (SHA-256 `C86335CB9251B65AEE2B3582DF1B77BFC7728B1C5D0F22999A38B964CA6D7136`) e o [JAR publicado](https://repo.maven.apache.org/maven2/io/github/johnpitter/cnpj-alfanumerico/1.1.0/cnpj-alfanumerico-1.1.0.jar) (SHA-256 `CEE063A8D45A32956BC7C834442214A973D4878BFF0B1EE303D830AE83928096`). Compilar `src/java` do ZIP com Java 8 e executar os dois programas Tester. Para cada linha, chamar `CNPJValidator.isValid` do ZIP, `CNPJ.isValid` e, se aceito, `CNPJ.unformat` do JAR. Em JDK 21, incluir classes do Core e Kotlin stdlib 2.3.10 e chamar `Cnpj.INSTANCE.normalizar`. Ler o bytecode da fachada publicada com `javap -verbose` (versão 52). O smoke de Kotlin e o fluxo HTTP completo pertencem à validação de adoção.
