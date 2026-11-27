package io.github.brasil4j.identidade.nis;

import io.github.brasil4j.identidade.DocumentParsers;
import io.github.brasil4j.verificacao.Modulus11;

import java.util.function.UnaryOperator;

/**
 * Configurações opcionais de conferência do NIS. O padrão é estrutural. As configurações com DV são
 * nomeadas pela fonte e pelo alcance, e nenhuma é a regra oficial do NIS em geral.
 */
public enum NisValidationMode {
    /** Exatamente 11 dígitos ASCII, sem máscara e sem DV. É o modo padrão, igual ao da Spec 006. */
    FORMAT_ONLY(builder -> builder),

    /**
     * Acrescenta o DV pela fórmula publicada no Caderno do Agente Financeiro v016 da Caixa, de alcance
     * documentado para PIS/Pasep no FGTS (ver {@link Modulus11#pisCaixaFgtsV016()}). Não inclui a guarda
     * de algarismos repetidos, que pode ser encadeada com {@link NisRules#repeatedDigits()}.
     */
    CAIXA_FGTS_V016(builder -> builder.checkDigits(Modulus11.pisCaixaFgtsV016())),

    /**
     * Acrescenta o DV pela convenção técnica observada em Declarações e UEM (ver
     * {@link Modulus11#pisLegacyMod11()}), com a guarda de algarismos repetidos desses legados.
     */
    LEGACY_MOD11(builder -> builder.checkDigits(Modulus11.pisLegacyMod11()).documentRule(NisRules.repeatedDigits()));

    private final UnaryOperator<DocumentParsers.Builder<Nis>> configuration;

    NisValidationMode(UnaryOperator<DocumentParsers.Builder<Nis>> configuration) {
        this.configuration = configuration;
    }

    DocumentParsers.Builder<Nis> configure(DocumentParsers.Builder<Nis> builder) {
        return configuration.apply(builder);
    }
}
