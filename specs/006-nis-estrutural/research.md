# Research · Conferência estrutural de NIS, PIS, Pasep e NIT

## D1 · Um valor textual com núcleo Java

- **Decisão.** Usar `Nis` no contexto Identidade Brasileira e manter uma única regra no núcleo Java. Usar uma value class Kotlin sobre o valor Java.
- **Motivo.** O S-2200 2.5 agrupa PIS, Pasep e NIT no campo `nisTrab`. CQC e CAGED sustentam as 11 posições numéricas. Preservar a string recebida mantém os zeros e habilita a recuperação explícita do texto. Os padrões locais de CPF já separam o núcleo da ergonomia Kotlin. [Leiautes oficiais](../../docs/descoberta/006-nis/leiautes-oficiais.md)
- **Alternativas avaliadas.** Tipos por rótulo exigiriam informação que o campo não carrega. Tipos numéricos perdem largura. Um validador público separado criaria um segundo caminho para a mesma invariante. A comparação entre linguagens usa a conversão explícita com `toJava()`.

## D2 · Gramática, falhas e privacidade

- **Decisão.** Conferir comprimento 11 e intervalo ASCII de `'0'` a `'9'` diretamente. Usar `IllegalArgumentException("NIS inválido.")` nas falhas e `"[NIS oculto]"` como representação pública padrão. A construção Kotlin recebe `String?` para encaminhar também campos extraídos que estejam ausentes. A extensão de conveniência tem receptor `String`.
- **Motivo.** Cada entrada pode ser conferida sem transformação ou dependência. O marcador neutro evita associar a saída ao leiaute visual de um formulário. O erro constante continua informativo sem incluir o campo recebido.
- **Alternativas avaliadas.** `Character.isDigit` inclui algarismos não ASCII. Trim, remoção de pontuação e máscara SIPREV mudariam o aceite. A fórmula da Caixa pertence ao processo PIS/Pasep documentado para FGTS e diverge dos legados no resto 1. [Matriz do C1](../../docs/descoberta/c1/nis-matriz.md) · [Pareceres e checagem](../../docs/descoberta/006-nis/revisao.md)

## D3 · Evidência histórica reproduzível

- **Decisão.** Compartilhar fixtures de teste entre os módulos pelo mecanismo de `testResources` já usado para CNPJ. Reproduzir a extração de `nisTrab` em fragmentos sintéticos com o namespace do S-2200 2.5 e chamar a API pública pelos JARs.
- **Motivo.** A sondagem do fragmento XSD local em `ca3b3d9` admitiu texto curto e letras. O artefato de teste precisa exercitar a fronteira onde o consumidor já extraiu o campo, incluindo ausência, vazio e espaços preservados pelo XML. A regra cadastral histórica do CNIS e do RET tem outra finalidade. [Sondagem e referências](../../docs/descoberta/006-nis/leiautes-oficiais.md)
- **Alternativas avaliadas.** Testes somente com strings cobririam a gramática sem exercitar a passagem pelo arquivo. Validar o evento completo exigiria dados e regras alheios à spec. A extração usa XML do JDK somente no código de teste, sem depender de checkout externo ou de serviço governamental.

## D4 · Compatibilidade dos artefatos

- **Decisão.** Comparar o candidato à tag `v0.2.0`, commit `8e94704689ac4a48b174a29ab264f0f5eba0164d`, com o script de compatibilidade e japicmp 0.26.1 já usados no projeto. Manter a checagem histórica de v0.1.0 já prevista no CI.
- **Motivo.** A v0.2.0 é a versão imediatamente anterior e inclui a API de Geografia. O script exercita compatibilidade binária e de código-fonte dos dois módulos com membros sintéticos, usando os JARs e classpaths correspondentes.
- **Alternativas avaliadas.** Limitar a comparação a v0.1.0 perderia a API introduzida na v0.2.0. Exigir JARs do Maven Central criaria uma dependência de publicação. A baseline pode ser reconstruída a partir do commit da tag.
