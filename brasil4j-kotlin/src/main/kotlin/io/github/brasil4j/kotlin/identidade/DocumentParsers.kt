package io.github.brasil4j.kotlin.identidade

import io.github.brasil4j.identidade.Document
import io.github.brasil4j.identidade.DocumentParser

/**
 * Interpreta [input] com qualquer interpretador do núcleo, inclusive de documentos do consumidor,
 * e devolve o documento Java ou `null` para entrada ausente ou rejeitada.
 */
public fun <D : Document> DocumentParser<D>.parseOrNull(input: String?): D? = parse(input).document.orElse(null)
