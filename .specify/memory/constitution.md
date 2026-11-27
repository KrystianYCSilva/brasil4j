# Constituição do brasil4j

## Core Principles

### I. SDD — contrato antes do código

Cada incremento define o comportamento em spec, detalha decisões no plano, deriva tarefas e passa por análise de consistência antes da implementação. Mudanças de API pública registram impacto e compatibilidade para os consumidores.

### II. DDD — invariantes e fronteiras

CNPJ e CPF são valores imutáveis de Identidade Brasileira. A regra fica no tipo que detém a invariante. Cadastro, titularidade e situação pertencem aos consumidores. Novos contextos exigem demanda identificada e um contrato próprio.

### III. TDD — uma invariante por ciclo

Escrever um teste determinístico de comportamento, observar a falha esperada, fazer a menor mudança que passe e revisar o desenho com a suíte verde. Registrar as três etapas. Testar bordas, modos e regressões. Exercitar o JAR como consumidor Java e Kotlin.

### IV. Plataforma e qualidade

O runtime mínimo é Java 8. Compilar contra APIs Java 8 e verificar bytecode 52, Maven Wrapper, Enforcer e execução em JDK 8 e 21. O núcleo depende apenas do JDK. O módulo Kotlin depende do núcleo e da biblioteca padrão. Aplicar JaCoCo a partir do domínio, com ≥90% de linhas e ≥85% de branches. Usar japicmp a partir da v0.2.

### V. Proveniência e escopo

Implementar de forma independente sob Apache-2.0. Registrar autoridade, revisão e evidência de cada regra em `docs/fontes.md`. Usar vetores oficiais ou sintéticos conferidos. Validar a apresentação antes da normalização, preservar zeros e explicitar tolerâncias. Ocultar CPF e NIS na representação textual padrão.

Só descrever uma regra como oficial com fonte normativa confirmada, seja nota técnica, portaria, instrução normativa, manual ou leiaute oficial, registrando autoridade, versão ou vigência e alcance. Sem essa confirmação, descrever a regra como fórmula publicada em um documento com o alcance nele indicado, ou como convenção técnica observada em sistemas identificados. Uma regra de alcance restrito ou não confirmado entra apenas como opção explícita do consumidor, nunca como comportamento padrão.

## Engenharia JVM

Aplicar Clean Code, KISS, DRY e SOLID pela legibilidade, responsabilidades coesas e uma fonte para cada regra de domínio. Preferir composição e encapsulamento.

A biblioteca oferece contratos públicos para valor, interpretação, regra, resultado e apresentação dos documentos. O reuso vem da composição e o comportamento varia por polimorfismo. Assim o consumidor consegue criar seus próprios documentos e regras no mesmo padrão dos tipos da biblioteca. Preferir estratégias injetáveis a classes utilitárias estáticas. Escolher padrões de projeto do catálogo GoF ([refactoring.guru](https://refactoring.guru/design-patterns)) pelo problema que resolvem e nomeá-los no plano. Cada padrão ou abstração precisa de justificativa por variação, acoplamento, duplicação ou extensibilidade concreta pelo consumidor.

Em Java, preservar os contratos OO e a compatibilidade 8. Em Kotlin, aproveitar null safety, imutabilidade, funções puras e APIs idiomáticas. Escolher laços ou composição funcional pela clareza. A adaptação Kotlin delega a regra ao núcleo Java. [Padrões de aplicação](../../.context/standards/jvm.md).

## Fluxo e verificação

A documentação pública é parte do contrato. Verificar JavaDoc com DocLint no build e KDoc com Dokka no `verify` em JDK 21. Conferir exemplos, erros e políticas públicas na revisão. Documentação gerada fica em diretórios de build. [Critérios de revisão](../../.context/standards/qualidade.md).

## Governance

- **Emendas.** Um escritor por rodada. Aplicar o procedimento do Spec Kit, registrar versão e impacto, e alinhar em etapa própria os artefatos versionados afetados. O README e o índice em `.context` orientam a leitura. Remover o relatório temporário de impacto antes do commit.
- **Versão.** MAJOR para remoção ou redefinição incompatível, MINOR para princípio ou orientação materialmente ampliados e PATCH para redação sem alteração de sentido.
- **Conformidade.** Revisar a constituição antes da pesquisa e após o desenho. Analisar spec, plano e tarefas antes da implementação. Justificar exceções no plano. Registrar evidência de execução, fontes e data ISO 8601.

**Version**: 0.5.0 | **Ratified**: 2026-11-26 | **Last Amended**: 2026-11-27
