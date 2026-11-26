# Contract: Bundle para Maven Central

- Entrada: reator 0.1.0, JDK 21, chave OpenPGP em `MAVEN_GPG_KEY`.
- Comando: `mvnw -Pcentral clean deploy`.
- Saída: `target/central-publishing/central-bundle.zip`. Estrutura Maven sob `io/github/brasil4j`, três POMs e seis JARs, com assinaturas `.asc` e checksums. Os binários principais têm bytecode 52.
- Publicação: `skipUpload` fixado no perfil. Chave inválida, JDK incompatível ou falha de qualidade interrompem a geração.
