# Research: Bundle para Maven Central

- O pai é referenciado por ambos os módulos. Incluir os três POMs no mesmo deploy local preserva a resolução do consumidor.
- Gerar JavaDoc e Dokka, anexar fontes e documentação no package, assinar no verify e empacotar no deploy. O perfil usa JDK 21 e o build regular conserva seu ciclo.
- Bouncy Castle recebe a chave por ambiente. O plugin oficial do Portal calcula checksums e, com `skipUpload`, encerra no ZIP local.
- Namespace GitHub automático segue o login. Para `io.github.brasil4j`, a prova depende da modalidade oferecida pelo Central Portal.

[Central](https://central.sonatype.org/publish/requirements/) · [Plugin de publicação](https://central.sonatype.org/publish/publish-portal-maven/) · [Maven GPG](https://maven.apache.org/plugins/maven-gpg-plugin/sign-mojo.html) · [Dokka](https://kotlinlang.org/docs/dokka-maven.html)
