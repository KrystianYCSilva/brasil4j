# Feature Specification: Módulo vitrine de exemplos executáveis de utilização (`brasil4j-exemplos`)

- **Feature Branch**: `009-exemplos-utilizacao`
- **Created**: 2026-11-27
- **Status**: Ready for implementation
- **Base**: Branch `008-nucleo-monetario` (`517280f`), isolada em worktree

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar exemplos didáticos em Java 8 para todos os domínios da biblioteca (Priority: P1)

Como visitante do repositório ou desenvolvedor avaliando adotar o `brasil4j` em um serviço Java, quero encontrar em um módulo dedicado de exemplos testes autoexplicativos demonstrando o uso das classes, métodos e modos de validação de Identidade (CNPJ, CPF, NIS), Geografia (Código IBGE de município e UF) e Finanças (Moedas, Arredondamentos, Real/Dólar/Euro, Rateio/Alocação e Taxa de Câmbio). Assim compreendo rapidamente o comportamento e copiando padrões corretos sem ler suítes internas de borda.

**Why this priority**: A principal porta de entrada para novos usuários Java do repositório é enxergar cenários reais de negócio funcionando ponta a ponta.

**Independent Test**: Executar as suítes de exemplo Java do módulo `brasil4j-exemplos` no build padrão e confirmar que cobrem todos os contextos públicos da biblioteca com nomes e comentários orientados ao leitor.

**Acceptance Scenarios**:

1. **Given** um visitante inspecionando `brasil4j-exemplos`, **When** abrir os pacotes de teste de Identidade, Geografia e Finanças em Java, **Then** encontrar exemplos claros e executáveis cobrindo modos de validação, formatação, privacidade em representação textual, consulta de UF/DDD/município, arredondamentos ABNT/Financeiro/Truncamento, parcelamento/alocação sem perda de centavos e câmbio reversível.
2. **Given** o ciclo de verificação padrão do projeto, **When** executado em ambos os ambientes suportados, **Then** compilar e aprovar automaticamente 100% dos exemplos, impedindo que exemplos fiquem defasados em relação à API pública.

---

### User Story 2 - Consultar exemplos idiomáticos em Kotlin (Priority: P2)

Como desenvolvedor Kotlin visitando o repositório, quero ver exemplos dedicados mostrando *value classes*, extensões de conversão/validação, sobrecarga de operadores monetários (`+`, `-`, `*` comutativo, `/`, `-` unário), alocação por pesos e interoperabilidade com o núcleo.

**Why this priority**: Evidencia a ergonomia diferencial do módulo `brasil4j-kotlin` lado a lado com o núcleo Java.

**Independent Test**: Executar as suítes Kotlin de `brasil4j-exemplos` e confirmar cobertura prática de Identidade, Geografia e Finanças.

**Acceptance Scenarios**:

1. **Given** as suítes Kotlin em `brasil4j-exemplos`, **When** executadas no build padrão, **Then** demonstrar a API idiomática Kotlin para CNPJ, CPF, NIS, Código Municipal/UF e operações monetárias/cambiais com 100% de aprovação.

---

### User Story 3 - Garantir que o módulo de exemplos nunca seja publicado nem infle o bundle Central (Priority: P3)

Como mantenedor responsável pelas releases no Maven Central, quero que o módulo `brasil4j-exemplos` contenha código apenas nos diretórios de teste e jamais seja incluído, assinado ou publicado no bundle de distribuição. Assim os artefatos publicados continuam restritos a `brasil4j` (pai), `brasil4j-core` e `brasil4j-kotlin`.

**Why this priority**: Preserva o contrato de publicação estabelecido na Spec 004 e impede poluição de coordenadas públicas no Maven Central.

**Independent Test**: Verificar que `brasil4j-exemplos` não possui código em `src/main`, que está desabilitado para instalação/deploy/assinatura e que a ativação do perfil `central` mantém apenas os módulos publicáveis no reator.

**Acceptance Scenarios**:

1. **Given** o perfil de publicação `central`, **When** inspecionada a lista efetiva de módulos ativos no reator sob `-Pcentral`, **Then** `brasil4j-exemplos` não participa da geração do bundle e possui travas explícitas de `skip` para deploy, GPG e fontes.

---

### Edge Cases

- Documentos sensíveis nos exemplos (CPF e NIS) utilizam exclusivamente vetores sintéticos ou públicos já homologados no repositório e demonstram explicitamente que `toString()` oculta o número (`***.***.***-**` para CPF e `[NIS oculto]` para NIS).
- Exemplos de falha esperada (ex.: entrada inválida, soma de moedas distintas, escala excedente no modo `exato`) demonstram como capturar/esperar `IllegalArgumentException` de forma didática.
- A ausência de classes em `src/main` em `brasil4j-exemplos` não deve causar falha nos plugins de cobertura (JaCoCo), documentação (JavaDoc/Dokka) ou checagem de bytecode (`check-bytecode.py`).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O repositório DEVE conter o módulo `brasil4j-exemplos` dedicado exclusivamente a exemplos executáveis localizados em pacotes de teste (`src/test/java` e `src/test/kotlin`), sem nenhum arquivo-fonte de produção em `src/main`.
- **FR-002**: O módulo `brasil4j-exemplos` DEVE estar ativo por padrão nas execuções regulares de build e verificação (em todas as versões de plataforma suportadas), compilando e executando os exemplos Java e Kotlin contra os artefatos do reator.
- **FR-003**: O módulo `brasil4j-exemplos` NÃO DEVE ser publicado nem incluído no bundle do perfil `central`, devendo combinar exclusão por perfil no reator com propriedades explícitas de desativação de deploy, instalação, assinatura GPG, geração de fontes, JavaDoc e cobertura de código de produção vazio.
- **FR-004**: Os pacotes de teste Java de `brasil4j-exemplos` DEVEM cobrir exemplos práticos e comentados para: (a) CNPJ (numérico, alfanumérico, somente formato, validador e máscara), CPF (formato padrão, validação numérica de DV e ocultação textual) e NIS (forma estrita com zeros à esquerda, política adicional do consumidor, formatador explícito e ocultação textual); (b) Código IBGE de município e UF (prefixo, correspondência com UF, busca tolerante por nome e consulta de sigla, capital, código IBGE e DDDs); e (c) Finanças (constantes e moedas ISO/customizadas, arredondamentos ABNT/Financeiro/Truncamento/customizado, `Real`/`Dolar`/`Euro`/`QuantiaMonetaria`, modo `exato`, centavos/unidades mínimas, `ratear`, `alocar` por Hamilton e `TaxaCambio` direta/inversa).
- **FR-005**: Os pacotes de teste Kotlin de `brasil4j-exemplos` DEVEM cobrir exemplos idiomáticos equivalentes para Identidade, Geografia e Finanças (incluindo *value classes*, extensões `.toCnpj()`, `.toCpf()`, `.toNis()`, `.toCodigoMunicipio()`, `.reais()`, `.dolares()`, `.euros()`, `alocarPorPesos` e operadores `+`, `-`, `*` comutativo, `/`, `-` unário e comparações).
- **FR-006**: O módulo `brasil4j-exemplos` DEVE possuir um `README.md` próprio funcionando como índice navegável para os visitantes do repositório, referenciado a partir do `README.md` da raiz.

### Key Entities

- **Módulo `brasil4j-exemplos`**: Submódulo Maven sem código de produção e sem deploy, contendo suítes de teste didáticas em Java e Kotlin e índice de navegação.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das famílias públicas entregues até a base atual (CNPJ, CPF, NIS, Código Municipal, UF, Moeda, Arredondamento, Dinheiro/Real/Dólar/Euro/QuantiaMonetaria, Rateio/Alocação e Taxa de Câmbio) possuem pelo menos um exemplo executável em Java e um em Kotlin dentro de `brasil4j-exemplos`.
- **SC-002**: 100% dos exemplos de `brasil4j-exemplos` executam com sucesso em `clean verify` nos dois ambientes de execução suportados, mantendo zero arquivos em `src/main` e zero impacto nos scripts `check-bytecode.py` e `check-api-compatibility.py`.
- **SC-003**: A ativação do perfil `-Pcentral` exclui `brasil4j-exemplos` do plano de execução do reator, preservando intacto o contrato de publicação de 3 POMs e 6 JARs da Spec 004.

## Assumptions

- Quando a `Spec 007` (*Composição de identidade*) for integrada em `main`, um teste adicional de exemplo para a composição poderá ser acrescentado ao pacote de exemplos de Identidade durante ou após a integração.
