package io.github.brasil4j.financas;

import java.util.Currency;
import java.util.Locale;

/**
 * Descritor monetário composto por código identificador ASCII visível, quantidade de casas
 * decimais ({@code 0..18}) e símbolo textual.
 *
 * <p>Consumidores podem usar as constantes prontas ({@link #BRL}, {@link #USD}, {@link #EUR}),
 * códigos ISO 4217 via {@link #iso(String, int)} ou implementar esta interface para unidades
 * customizadas. Valores monetários capturam um instantâneo imutável ao construir instâncias.
 */
public interface Moeda {
    /** Real brasileiro ({@code BRL}, {@code 2} casas decimais, símbolo {@code R$}). */
    Moeda BRL = ValidacaoMoeda.criarConstanteInicial("BRL", "R$");

    /** Dólar americano ({@code USD}, {@code 2} casas decimais, símbolo {@code US$}). */
    Moeda USD = ValidacaoMoeda.criarConstanteInicial("USD", "US$");

    /** Euro ({@code EUR}, {@code 2} casas decimais, símbolo {@code €}). */
    Moeda EUR = ValidacaoMoeda.criarConstanteInicial("EUR", "€");

    /**
     * Retorna o código identificador ASCII visível ({@code 1..16} caracteres, sem espaços).
     *
     * @return código da moeda
     */
    String getCodigo();

    /**
     * Retorna a quantidade de casas decimais da moeda ({@code 0..18}).
     *
     * @return quantidade de casas decimais
     */
    int getCasasDecimais();

    /**
     * Retorna o símbolo textual da moeda ({@code 1..16} caracteres, sem controle ou formatação invisível).
     * Por padrão devolve {@link #getCodigo()}.
     *
     * @return símbolo da moeda
     */
    default String getSimbolo() {
        return getCodigo();
    }

    /**
     * Cria ou recupera uma moeda ISO 4217 validada com símbolo igual ao código (ou a constante
     * canônica para {@code BRL/2}, {@code USD/2} e {@code EUR/2}).
     *
     * @param codigo código de três letras maiúsculas ASCII ({@code [A-Z]{3}})
     * @param casasDecimais quantidade de casas decimais ({@code 0..18})
     * @return instância imutável de moeda
     * @throws IllegalArgumentException se o código for nulo/inválido ou as casas estiverem fora de {@code 0..18}
     */
    static Moeda iso(String codigo, int casasDecimais) {
        return ValidacaoMoeda.criarIso(codigo, casasDecimais);
    }

    /**
     * Cria uma moeda imutável com código, casas decimais e símbolo explícitos.
     *
     * @param codigo código ASCII visível ({@code 1..16} caracteres, sem espaços)
     * @param casasDecimais quantidade de casas decimais ({@code 0..18})
     * @param simbolo símbolo textual ({@code 1..16} caracteres, sem caracteres de controle ou formatação)
     * @return instância imutável de moeda
     * @throws IllegalArgumentException se qualquer argumento for nulo ou violar os limites
     */
    static Moeda of(String codigo, int casasDecimais, String simbolo) {
        return ValidacaoMoeda.criarGeral(codigo, casasDecimais, simbolo);
    }

    /**
     * Adapta uma instância {@link Currency} do JDK fixando explicitamente as casas decimais e
     * obtendo o símbolo neutro em {@link Locale#ROOT} para determinismo entre ambientes.
     *
     * @param currency instância não nula de {@link Currency}
     * @param casasDecimais quantidade de casas decimais ({@code 0..18})
     * @return instância imutável de moeda
     * @throws IllegalArgumentException se {@code currency} for nula ou as casas estiverem fora de {@code 0..18}
     */
    static Moeda fromJdkCurrency(Currency currency, int casasDecimais) {
        if (currency == null) {
            throw new IllegalArgumentException("A moeda do JDK não pode ser nula.");
        }
        return ValidacaoMoeda.criarGeral(
                currency.getCurrencyCode(), casasDecimais, currency.getSymbol(Locale.ROOT));
    }

    /**
     * Confere se duas referências representam a mesma moeda contábil pelo par
     * {@code (codigo, casasDecimais)}, sem considerar o símbolo e devolvendo {@code false}
     * para referências nulas ou com código/casas inválidos.
     *
     * @param a primeira moeda (pode ser nula)
     * @param b segunda moeda (pode ser nula)
     * @return {@code true} se ambas tiverem código ASCII válido idêntico e mesmas casas ({@code 0..18}); {@code false} caso contrário
     */
    static boolean mesmaMoeda(Moeda a, Moeda b) {
        return ValidacaoMoeda.mesmaMoeda(a, b);
    }
}
