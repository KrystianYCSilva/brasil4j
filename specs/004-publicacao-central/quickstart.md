# Quickstart: publicação Central

## Bundle local

Use JDK 21. Injete `MAVEN_GPG_KEY` e, para uma chave protegida, `MAVEN_GPG_PASSPHRASE` por um gerenciador de segredos ou pelo ambiente do processo. O assinador espera a chave privada OpenPGP transferível em formato ASCII armored. A chave de teste demonstra o fluxo. Use uma chave do mantenedor, com chave pública distribuída, para uma futura publicação.

Em `main`, valide assinatura e anexos da versão `0.2.0-SNAPSHOT` com `-Pcentral clean verify`.

Para gerar o ZIP, trabalhe em uma cópia com as versões de release alinhadas no POM pai e nos dois módulos.

```powershell
.\mvnw.cmd -B -ntp -Pcentral clean deploy
```

Na cópia de release, o ZIP fica em `target/central-publishing/central-bundle.zip`. Confira os três GAVs, fontes, documentação, checksums, bytecode e assinaturas com a chave pública. O perfil fixa `skipUpload=true` e `autoPublish=false`.

O bundle histórico da v0.1.0 foi gerado antes do incremento de IBGE/UF. Use a revisão de origem daquele bundle para reproduzi-lo. A referência `v0.1.0-central` fica reservada para identificar essa revisão quando a publicação for retomada.

## Namespace

1. Entre no [Central Portal](https://central.sonatype.com/publishing/namespaces) com a sua conta e selecione **View Namespaces → Add Namespace**. Solicite `io.github.brasil4j`.
2. Se o Portal oferecer verificação por hospedagem, copie a chave de verificação. Crie um repositório público temporário com esse nome exato sob o proprietário GitHub `brasil4j`. Então acione **Verify Namespace**.
3. Aguarde **Verified**. Se a conta atual não controla esse proprietário, se o namespace já estiver verificado por terceiros ou se o Portal não oferecer a prova, leve ao [Central Support](mailto:central-support@sonatype.com) o namespace exato, o [repositório atual](https://github.com/KrystianYCSilva/brasil4j) e o estado mostrado no Portal. O namespace pessoal automático é independente desse pedido.
4. Após verificar o namespace e confirmar que os GAVs estão disponíveis, gere um bundle com a chave do publicador. Validação e publicação no Portal são etapas posteriores.

[Registro e verificação](https://central.sonatype.org/register/namespace/) · [Assinaturas e chave pública](https://central.sonatype.org/publish/requirements/gpg/)
