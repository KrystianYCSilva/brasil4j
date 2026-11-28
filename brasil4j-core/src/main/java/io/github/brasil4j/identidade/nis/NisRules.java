package io.github.brasil4j.identidade.nis;

import io.github.brasil4j.identidade.Rejection;
import io.github.brasil4j.identidade.RejectionReason;
import io.github.brasil4j.identidade.ValidationRule;
import io.github.brasil4j.verificacao.CheckDigitAlgorithm;

import java.util.Optional;

/**
 * Regras opcionais para compor com o interpretador estrutural do NIS por
 * {@link io.github.brasil4j.identidade.DocumentParser#withRule(ValidationRule)}.
 * Cada regra é nomeada pela fonte e pelo alcance, e nenhuma é a regra oficial do NIS em geral.
 */
public final class NisRules {
    private static final Rejection REPEATED_DIGITS =
            Rejection.of(RejectionReason.INVALID_CHECK_DIGITS, "REPEATED_DIGITS");

    private NisRules() {
    }

    /**
     * Rejeita inscrições com os onze algarismos iguais, como prevê a RV_03 das Regras de Validação v1.3
     * do SIPREV Gestão (Dataprev), cujo alcance é o SIPREV. A rejeição tem motivo
     * {@link RejectionReason#INVALID_CHECK_DIGITS} e código {@code REPEATED_DIGITS}.
     *
     * @return regra de algarismos repetidos
     */
    public static ValidationRule<Nis> repeatedDigits() {
        ValidationRule<String> canonical = repeatedDigitsOfCanonical();
        return nis -> canonical.check(nis.getCanonicalValue());
    }

    /**
     * Confere os dígitos verificadores da inscrição pelo algoritmo informado, por exemplo um preset de
     * {@link io.github.brasil4j.verificacao.Modulus11} ou um algoritmo do consumidor. Um algoritmo cujo
     * domínio não aceita a inscrição gera {@link RejectionReason#INVALID_FORMAT}.
     *
     * @param algorithm algoritmo de dígitos verificadores
     * @return regra de dígitos verificadores
     * @throws NullPointerException se o algoritmo for nulo
     */
    public static ValidationRule<Nis> checkDigits(CheckDigitAlgorithm algorithm) {
        ValidationRule<String> canonical = ValidationRule.checkDigits(algorithm);
        return nis -> canonical.check(nis.getCanonicalValue());
    }

    /** Guarda de repetição sobre o canônico, usada antes do cálculo na convenção legada. */
    static ValidationRule<String> repeatedDigitsOfCanonical() {
        return canonical -> {
            for (int index = 1; index < canonical.length(); index++) {
                if (canonical.charAt(index) != canonical.charAt(0)) {
                    return Optional.empty();
                }
            }
            return Optional.of(REPEATED_DIGITS);
        };
    }
}
