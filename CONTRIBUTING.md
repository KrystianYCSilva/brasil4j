# Contribuindo com o brasil4j

Leia a [constituição](.specify/memory/constitution.md) e use o [índice de engenharia](.context/README.md) para abrir só o padrão relevante. As contribuições seguem a [licença Apache-2.0](LICENSE).

## Proponha uma mudança

1. Descreva o problema, quem usa a API e um exemplo do comportamento esperado. Para uma regressão, inclua uma reprodução com dados sintéticos.
2. Use o Spec Kit para manter spec, plano, tarefas e análise alinhados. Atualize o contrato e o quickstart se a mudança afetar o consumidor.
3. Para regras brasileiras, registre autoridade, revisão e vetor em [`docs/fontes.md`](docs/fontes.md). Use exemplos oficiais ou dados sintéticos conferidos.

## Implemente e teste

- Mantenha as regras e invariantes no núcleo Java, compatível com Java 8. O módulo Kotlin delega ao núcleo e oferece uma API idiomática. [Padrões JVM](.context/standards/jvm.md)
- Escreva primeiro um teste que falhe pelo comportamento esperado, aplique a menor correção e refatore com a suíte verde. Cubra entradas inválidas, fronteiras, modos e consumo pelo JAR quando o contrato mudar. [Padrões de qualidade](.context/standards/qualidade.md)
- Documente APIs públicas em JavaDoc ou KDoc. Explique valores padrão, retorno, falhas e limites. Use dados sintéticos nos exemplos e mantenha credenciais em variáveis de ambiente.

## Verifique antes de enviar

Rode o Maven Wrapper com **JDK 8 e JDK 21**. O JDK escolhido deve estar disponível em `JAVA_HOME`.

```powershell
.\mvnw.cmd -B -ntp clean verify
python scripts/check-bytecode.py
```

```sh
./mvnw -B -ntp clean verify
python3 scripts/check-bytecode.py
```

O build verifica testes, cobertura e JavaDoc. O JDK 21 também executa Dokka para conferir KDoc. No pedido de revisão, informe o comportamento alterado, o impacto na API e quais verificações foram executadas. Confira o conjunto do Git e mantenha temporários, saídas de build e arquivos locais de IA fora da contribuição.
