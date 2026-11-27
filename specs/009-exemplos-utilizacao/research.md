# Research · Spec 009 — Módulo vitrine `brasil4j-exemplos`

## D1 · Isolamento de publicação (`-Pcentral`) com execução contínua no build padrão

- **Decision**: Declarar `<module>brasil4j-exemplos</module>` em um perfil `<id>exemplos</id>` com `<activeByDefault>true</activeByDefault>` no `pom.xml` raiz e definir em `brasil4j-exemplos/pom.xml` as propriedades `maven.deploy.skip=true`, `maven.install.skip=true`, `gpg.skip=true`, `maven.source.skip=true`, `maven.javadoc.skip=true` e `jacoco.skip=true`.
- **Rationale**:
  1. Em `mvnw clean verify` (JDK 8 e JDK 21), nenhum perfil do POM raiz é ativado por linha de comando, mantendo `exemplos` ativo automaticamente para compilar e testar 100% dos exemplos em todo commit/CI.
  2. Em `mvnw -Pcentral clean deploy`, a ativação explícita de `central` no POM raiz desativa automaticamente `activeByDefault`, excluindo `brasil4j-exemplos` do reator de publicação e preservando o bundle exato de 3 POMs + 6 JARs da Spec 004 (`SC-001`). Como bloqueio rígido contra `-Pcentral,exemplos`, `brasil4j-exemplos/pom.xml` também declara um perfil `<id>central</id>` com `maven-enforcer-plugin` (`<alwaysFail>`), além das flags `*.skip=true` como defesa em profundidade. E no CI (`.github/workflows/verify.yml`), um passo explícito verifica a geração de `brasil4j-exemplos/target/surefire-reports/TEST-*.xml` nas duas JDKs para detectar imediatamente qualquer desativação acidental de `activeByDefault`.
  3. **Nota sobre o POM pai publicado**: O arquivo `brasil4j-0.2.0.pom` publicado passa a conter o bloco `<profile><id>exemplos</id>...` listando `<module>brasil4j-exemplos</module>`. Essa exposição é aceita conscientemente: em Maven, `<modules>` de agregação nunca é herdado nem transitivo para consumidores de `brasil4j-core` ou `brasil4j-kotlin`, não cria coordenada publicada e mantém o build local simples sem POM agregador separado.
- **Alternatives considered**:
  - Incluir `brasil4j-exemplos` diretamente em `<modules>` raiz: rejeitado porque deixaria referência ao módulo não publicado dentro do `brasil4j-0.2.0.pom` enviado ao Maven Central e poderia acionar empacotamento vazio no `central-publishing-maven-plugin`.

## D2 · Organização de exemplos Java 8 e Kotlin em um único módulo somente de testes

- **Decision**: Estruturar `brasil4j-exemplos` sem diretório `src/main`, com exemplos Java 8 em `src/test/java/io/github/brasil4j/exemplos/java/` e exemplos Kotlin em `src/test/kotlin/io/github/brasil4j/exemplos/kotlin/`, compilados para `jvmTarget = 1.8` (`-Xjdk-release=8`) e executados pelo JUnit Jupiter `5.12.2` via Surefire.
- **Rationale**: Atende literalmente ao objetivo de expor exemplos apenas em pacotes de testes sem produzir classes de produção e reúne Java e Kotlin lado a lado em um único diretório navegável para quem visita o repositório no GitHub.
- **Alternatives considered**: Dois módulos separados de exemplo (`exemplos-java` e `exemplos-kotlin`) — rejeitado por pulverização desnecessária (KISS).
