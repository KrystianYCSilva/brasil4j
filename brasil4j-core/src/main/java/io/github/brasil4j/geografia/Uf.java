package io.github.brasil4j.geografia;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Dados de referência das unidades federativas brasileiras.
 */
public enum Uf {
    /** Rondônia. */
    RO("11", "Rondônia", "Porto Velho", "69"),
    /** Acre. */
    AC("12", "Acre", "Rio Branco", "68"),
    /** Amazonas. */
    AM("13", "Amazonas", "Manaus", "92", "97"),
    /** Roraima. */
    RR("14", "Roraima", "Boa Vista", "95"),
    /** Pará. */
    PA("15", "Pará", "Belém", "91", "93", "94"),
    /** Amapá. */
    AP("16", "Amapá", "Macapá", "96"),
    /** Tocantins. */
    TO("17", "Tocantins", "Palmas", "63"),
    /** Maranhão. */
    MA("21", "Maranhão", "São Luís", "98", "99"),
    /** Piauí. */
    PI("22", "Piauí", "Teresina", "86", "89"),
    /** Ceará. */
    CE("23", "Ceará", "Fortaleza", "85", "88"),
    /** Rio Grande do Norte. */
    RN("24", "Rio Grande do Norte", "Natal", "84"),
    /** Paraíba. */
    PB("25", "Paraíba", "João Pessoa", "83"),
    /** Pernambuco. */
    PE("26", "Pernambuco", "Recife", "81", "87"),
    /** Alagoas. */
    AL("27", "Alagoas", "Maceió", "82"),
    /** Sergipe. */
    SE("28", "Sergipe", "Aracaju", "79"),
    /** Bahia. */
    BA("29", "Bahia", "Salvador", "71", "73", "74", "75", "77"),
    /** Minas Gerais. */
    MG("31", "Minas Gerais", "Belo Horizonte", "31", "32", "33", "34", "35", "37", "38"),
    /** Espírito Santo. */
    ES("32", "Espírito Santo", "Vitória", "27", "28"),
    /** Rio de Janeiro. */
    RJ("33", "Rio de Janeiro", "Rio de Janeiro", "21", "22", "24"),
    /** São Paulo. */
    SP("35", "São Paulo", "São Paulo", "11", "12", "13", "14", "15", "16", "17", "18", "19"),
    /** Paraná. */
    PR("41", "Paraná", "Curitiba", "41", "42", "43", "44", "45", "46"),
    /** Santa Catarina. */
    SC("42", "Santa Catarina", "Florianópolis", "47", "48", "49"),
    /** Rio Grande do Sul. */
    RS("43", "Rio Grande do Sul", "Porto Alegre", "51", "53", "54", "55"),
    /** Mato Grosso do Sul. */
    MS("50", "Mato Grosso do Sul", "Campo Grande", "67"),
    /** Mato Grosso. */
    MT("51", "Mato Grosso", "Cuiabá", "65", "66"),
    /** Goiás. */
    GO("52", "Goiás", "Goiânia", "61", "62", "64"),
    /** Distrito Federal. */
    DF("53", "Distrito Federal", "Brasília", "61");

    private static final Pattern DIACRITICOS = Pattern.compile("\\p{M}+");
    private static final Map<String, Uf> POR_CODIGO_IBGE = indexarPorCodigoIbge();
    private static final Map<String, Uf> POR_NOME = indexarPorNome();

    private final String codigoIbge;
    private final String nome;
    private final String capital;
    private final List<String> ddds;

    Uf(String codigoIbge, String nome, String capital, String... ddds) {
        this.codigoIbge = codigoIbge;
        this.nome = nome;
        this.capital = capital;
        this.ddds = Collections.unmodifiableList(Arrays.asList(ddds));
    }

    /**
     * Retorna o código IBGE textual desta unidade federativa.
     *
     * @return código de dois dígitos
     */
    public String getIbgeCode() {
        return codigoIbge;
    }

    /**
     * Procura uma unidade federativa pelo código exato, sem normalização.
     *
     * @param codigo código textual do IBGE
     * @return a UF associada ou vazio para entrada nula, desconhecida ou em outra apresentação
     */
    public static Optional<Uf> fromIbgeCode(String codigo) {
        return Optional.ofNullable(POR_CODIGO_IBGE.get(codigo));
    }

    /**
     * Retorna o nome oficial da unidade federativa.
     *
     * @return nome completo, preservando a grafia oficial
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna a sigla desta unidade federativa.
     *
     * @return duas letras maiúsculas
     */
    public String getSigla() {
        return name();
    }

    /**
     * Retorna o nome da capital desta unidade federativa.
     *
     * @return nome da capital com sua grafia oficial
     */
    public String getCapital() {
        return capital;
    }

    /**
     * Retorna os códigos nacionais de telefonia atribuídos a localidades desta UF.
     * Um código pode aparecer em outra unidade federativa.
     *
     * @return lista imutável de DDDs de dois dígitos, em ordem crescente
     */
    public List<String> getDdds() {
        return ddds;
    }

    /**
     * Busca pelo nome completo. Desconsidera caixa, acentos e espaços nas extremidades.
     *
     * @param nome nome completo da unidade federativa
     * @return a UF encontrada ou vazio quando a entrada é nula ou não corresponde a um nome
     */
    public static Optional<Uf> fromName(String nome) {
        return nome == null ? Optional.empty() : Optional.ofNullable(POR_NOME.get(normalizarNome(nome)));
    }

    private static Map<String, Uf> indexarPorNome() {
        Map<String, Uf> indice = new HashMap<>();
        for (Uf uf : values()) {
            indice.put(normalizarNome(uf.nome), uf);
        }
        return Collections.unmodifiableMap(indice);
    }

    private static String normalizarNome(String nome) {
        String decomposto = Normalizer.normalize(nome.trim(), Normalizer.Form.NFD);
        return DIACRITICOS.matcher(decomposto).replaceAll("").toUpperCase(Locale.ROOT);
    }

    private static Map<String, Uf> indexarPorCodigoIbge() {
        Map<String, Uf> indice = new HashMap<>();
        for (Uf uf : values()) {
            indice.put(uf.codigoIbge, uf);
        }
        return Collections.unmodifiableMap(indice);
    }
}
