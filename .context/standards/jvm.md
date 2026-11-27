# Padrões JVM

- **Java 8.** Encarregue o objeto de valor de suas invariantes. Prefira composição, estado final e helpers com visibilidade mínima. Use Strategy para políticas intercambiáveis. Compartilhe cálculo, mantenha validação de entrada na estratégia.
- **Contratos e extensão.** Exponha interfaces para valor, interpretação, regra, resultado e apresentação, e construa os tipos da biblioteca sobre elas, como faria um consumidor. Rejeição esperada é resultado, não exceção nem `null`. Prefira estratégias injetáveis a utilitários estáticos. Mantenha utilitários publicados como fachadas deprecadas. Nomeie no plano cada padrão GoF usado e o problema que ele resolve.
- **Kotlin.** Modele ausência com nulabilidade explícita. Prefira `val`, funções puras e coleções somente leitura. Use extensões e value classes para ergonomia. Delegue ao Java. Escolha laço ou operações funcionais pelo fluxo mais claro.
- **Clean Code, KISS, DRY, SOLID.** Dê nomes que revelem intenção, agrupe uma responsabilidade por tipo e extraia abstrações quando houver regra ou variação comum. Preserve contratos observáveis ao refatorar.
- **Arquitetura.** Regra de Identidade Brasileira no núcleo, sintaxe de consumo no adaptador. Dados de cadastro e efeitos externos pertencem a consumidores.
- **Biblioteca.** Considere alteração de assinatura, valor canônico, erro, igualdade e apresentação como mudanças de API. Registre compatibilidade na spec.
