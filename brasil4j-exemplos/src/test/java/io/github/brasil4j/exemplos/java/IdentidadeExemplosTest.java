package io.github.brasil4j.exemplos.java;

import io.github.brasil4j.identidade.DocumentFormatter;
import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjType;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.composicao.DocumentCheck;
import io.github.brasil4j.identidade.composicao.DocumentCheckResult;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.nis.Nis;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exemplos completos e casos de uso reais do contexto de Identidade Brasileira em Java 8
 * ({@link Cnpj}, {@link Cpf} e {@link Nis} + validadores, modos e formatadores).
 *
 * <p>Todos os documentos utilizados abaixo são vetores sintéticos ou exemplos públicos de manuais técnicos.
 */
@DisplayName("Exemplos Java · Identidade Brasileira (CNPJ, CPF e NIS)")
class IdentidadeExemplosTest {

    @Test
    @DisplayName("CNPJ: validação padrão (numérica e alfanumérica), modos restritos, inspeção e formatação")
    void exemploCnpjNumericoAlfanumericoEModosDeValidacao() {
        // 1. O modo padrão (ALPHANUMERIC) aceita tanto o CNPJ numérico tradicional quanto o alfanumérico (IN RFB 2.229/2024)
        Cnpj cnpjNumerico = Cnpj.parse("11.222.333/0001-81");
        assertEquals("11222333000181", cnpjNumerico.getCanonicalValue());
        assertEquals("112223330001", cnpjNumerico.getBase());
        assertEquals("11222333", cnpjNumerico.getRoot());
        assertEquals("0001", cnpjNumerico.getBranch());
        assertEquals("81", cnpjNumerico.getCheckDigits());
        assertEquals(CnpjType.NUMERIC, cnpjNumerico.getType());
        assertEquals("11222333000181", cnpjNumerico.toString()); // pessoa jurídica expõe o valor canônico

        // 2. Vetor alfanumérico publicado no manual técnico da Receita Federal
        Cnpj cnpjAlfanumerico = Cnpj.parse("12ABC34501DE35");
        assertEquals("12.ABC.345/01DE-35", cnpjAlfanumerico.applyMask());
        assertEquals("12ABC34501DE", cnpjAlfanumerico.getBase());
        assertEquals("12ABC345", cnpjAlfanumerico.getRoot());
        assertEquals("01DE", cnpjAlfanumerico.getBranch());
        assertEquals("35", cnpjAlfanumerico.getCheckDigits());
        assertEquals(CnpjType.ALPHANUMERIC, cnpjAlfanumerico.getType());

        // 3. O interpretador trata null com segurança (false) e permite restringir sistemas legados a NUMERIC:
        assertFalse(Cnpj.parser().isValid(null));
        assertTrue(Cnpj.parser().isValid("12.ABC.345/01DE-35"));
        assertTrue(Cnpj.parser(CnpjValidationMode.NUMERIC).isValid("11.222.333/0001-81"));
        assertFalse(Cnpj.parser(CnpjValidationMode.NUMERIC).isValid("12.ABC.345/01DE-35"));

        // 4. Modo FORMAT_ONLY confere estrutura/apresentação sem validar o dígito verificador:
        Cnpj apenasFormato = Cnpj.parse("12ABC34501DE99", CnpjValidationMode.FORMAT_ONLY);
        assertEquals("12.ABC.345/01DE-99", apenasFormato.applyMask());
        assertThrows(IllegalArgumentException.class, () -> Cnpj.parse("12ABC34501DE99"));

        // 5. Apresentação e valor canônico disponíveis no próprio documento:
        assertEquals("12.ABC.345/01DE-35", Cnpj.parse("12ABC34501DE35").applyMask());
        assertEquals("12ABC34501DE35", Cnpj.parse("12.ABC.345/01DE-35").getCanonicalValue());
    }

    @Test
    @DisplayName("Caso de uso CNPJ: deduplicação em Set/Map e agrupamento de matriz e filiais pela raiz")
    void exemploAgrupamentoDeMatrizEFiliaisPelaRaizDoCnpj() {
        // Entradas com e sem máscara do mesmo estabelecimento colapsam na mesma chave em Set/Map:
        Set<Cnpj> fornecedoresUnicos = new HashSet<>();
        fornecedoresUnicos.add(Cnpj.parse("11.222.333/0001-81"));
        fornecedoresUnicos.add(Cnpj.parse("11222333000181"));
        assertEquals(1, fornecedoresUnicos.size());

        // Agrupamento corporativo de estabelecimentos (matriz + filiais numéricas e alfanuméricas) por getRoot():
        List<Cnpj> estabelecimentos = Arrays.asList(
                Cnpj.parse("11.222.333/0001-81"), // empresa A - matriz
                Cnpj.parse("11.222.333/0002-62"), // empresa A - filial 0002
                Cnpj.parse("12.ABC.345/01DE-35"), // empresa B - unidade 01DE
                Cnpj.parse("12.ABC.345/02DE-06")  // empresa B - unidade 02DE
        );

        Map<String, List<Cnpj>> porGrupoEmpresarial = new LinkedHashMap<>();
        for (Cnpj cnpj : estabelecimentos) {
            porGrupoEmpresarial.computeIfAbsent(cnpj.getRoot(), r -> new ArrayList<>()).add(cnpj);
        }

        assertEquals(2, porGrupoEmpresarial.size());
        assertEquals(2, porGrupoEmpresarial.get("11222333").size());
        assertEquals("0001", porGrupoEmpresarial.get("11222333").get(0).getBranch());
        assertEquals("0002", porGrupoEmpresarial.get("11222333").get(1).getBranch());
        assertEquals(2, porGrupoEmpresarial.get("12ABC345").size());
    }

    @Test
    @DisplayName("CPF: modo padrão de formato, conferência opcional de DV, zeros à esquerda e máscara")
    void exemploCpfModosZerosAEsquerdaEFormatacao() {
        // 1. Por padrão (FORMAT_ONLY), Cpf.parse valida tamanho/apresentação sem descartar registros por DV:
        Cpf cpfComDvDivergente = Cpf.parse("280.012.389-39");
        assertEquals("28001238939", cpfComDvDivergente.getCanonicalValue());
        assertFalse(cpfComDvDivergente.hasValidCheckDigits());
        assertTrue(Cpf.parser().isValid("280.012.389-39")); // FORMAT_ONLY padrão
        assertFalse(Cpf.parser().isValid(null));

        // 2. Para exigir conferência matemática pelo módulo 11 na entrada, use CpfValidationMode.NUMERIC:
        Cpf cpfValidoMatematicamente = Cpf.parse("28001238938", CpfValidationMode.NUMERIC);
        assertTrue(cpfValidoMatematicamente.hasValidCheckDigits());
        assertEquals("280012389", cpfValidoMatematicamente.getBase());
        assertEquals("38", cpfValidoMatematicamente.getCheckDigits());
        assertEquals("280.012.389-38", cpfValidoMatematicamente.applyMask());
        assertFalse(Cpf.parser(CpfValidationMode.NUMERIC).isValid("280.012.389-39"));

        // 3. Preservação integral de zeros à esquerda e formatação/desformatação direta:
        Cpf cpfComZerosIniciais = Cpf.parse("000.123.456-01", CpfValidationMode.NUMERIC);
        assertEquals("00012345601", cpfComZerosIniciais.getCanonicalValue());
        assertEquals("000.123.456-01", Cpf.parse("00012345601").applyMask());
        assertEquals("00012345601", Cpf.parse("000.123.456-01").getCanonicalValue());
    }

    @Test
    @DisplayName("Caso de uso CPF: triagem de migração de base legada e proteção automática em logs (LGPD)")
    void exemploCpfTriagemDeBaseLegadaEConformidadeLgpd() {
        // Em cargas de sistemas antigos, carrega-se o lote no modo estrutural e separa-se quem exige saneamento:
        List<String> loteLegado = Arrays.asList("280.012.389-38", "28001238938", "280.012.389-39", "00012345601");

        Set<Cpf> aprovadosAutomaticamente = new HashSet<>();
        List<Cpf> quarentenaDvDivergente = new ArrayList<>();

        for (String bruto : loteLegado) {
            Cpf cpf = Cpf.parse(bruto); // aceita com ou sem máscara
            if (cpf.hasValidCheckDigits()) {
                aprovadosAutomaticamente.add(cpf); // deduplica "280.012.389-38" e "28001238938"
            } else {
                quarentenaDvDivergente.add(cpf);
            }
        }

        assertEquals(2, aprovadosAutomaticamente.size());
        assertEquals(1, quarentenaDvDivergente.size());

        // Concatenação acidental em logs ou mensagens de erro NUNCA vaza os dígitos pessoais:
        Cpf emQuarentena = quarentenaDvDivergente.get(0);
        String mensagemLog = "Registro encaminhado para revisão cadastral: titular=" + emQuarentena;
        assertEquals("Registro encaminhado para revisão cadastral: titular=***.***.***-**", mensagemLog);
    }

    @Test
    @DisplayName("NIS/PIS/Pasep/NIT: escopo estritamente estrutural, política módulo 11 do consumidor e formatação")
    void exemploNisFolhaDePagamentoComPoliticasEFormatadores() {
        // 1. Por contrato, Nis valida APENAS a estrutura de 11 dígitos ASCII (sem conferir dígito verificador por padrão):
        assertFalse(Nis.parser().isValid(null));
        assertTrue(Nis.parser().isValid("00000000280"));
        assertTrue(Nis.parser().isValid("00000000281")); // estruturalmente válido (11 dígitos), mesmo com final diferente!
        assertFalse(Nis.parser().isValid("000.00000.28-0")); // máscara não é forma canônica de entrada

        Nis inscricao = Nis.parse("00000000280");
        assertEquals("00000000280", inscricao.getCanonicalValue());
        assertEquals("[NIS oculto]", inscricao.toString());

        // Igualdade canônica em coleções:
        Set<Nis> folha = new HashSet<>(Arrays.asList(Nis.parse("00000000280"), Nis.parse("00000000280")));
        assertEquals(1, folha.size());

        // 2. Quando o leiaute exigir módulo 11 (pesos 3,2,9,8,7,6,5,4,3,2) ou regras próprias, o consumidor compõe Predicate<Nis>:
        Predicate<Nis> naoTodosDigitosIguais = nis -> nis.getCanonicalValue().chars().distinct().count() > 1;
        Predicate<Nis> moduloOnzePis = nis -> {
            int[] pesos = {3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            String d = nis.getCanonicalValue();
            int soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (d.charAt(i) - '0') * pesos[i];
            }
            int resto = 11 - (soma % 11);
            int dvEsperado = resto >= 10 ? 0 : resto;
            return (d.charAt(10) - '0') == dvEsperado;
        };
        Predicate<Nis> politicaFolha = naoTodosDigitosIguais.and(moduloOnzePis);

        DocumentParser<Nis> interpretadorFolha = Nis.parser().withPolicy(politicaFolha);
        assertTrue(interpretadorFolha.isValid("00000000280"));
        assertFalse(interpretadorFolha.isValid("00000000281")); // rejeitado pela política de módulo 11 do consumidor
        assertFalse(interpretadorFolha.isValid("00000000000"));

        // 3. Apresentações distintas definidas explicitamente pelo consumidor conforme o canal de saída:
        DocumentFormatter<Nis> mascaraHolerite = n -> {
            String d = n.getCanonicalValue();
            return d.substring(0, 3) + "." + d.substring(3, 8) + "." + d.substring(8, 10) + "-" + d.substring(10);
        };
        DocumentFormatter<Nis> mascaraParcialPortal = n -> {
            String d = n.getCanonicalValue();
            return "***." + d.substring(3, 8) + ".**-" + d.substring(10);
        };

        assertEquals("000.00000.28-0", mascaraHolerite.apply(inscricao));
        assertEquals("***.00000.**-0", mascaraParcialPortal.apply(inscricao));
        assertEquals("[NIS oculto]", inscricao.toString()); // continua oculto após a formatação!
    }

    @Test
    @DisplayName("Composição heterogênea (Spec 007): lote de CPF, CNPJ e NIS avaliado uniformemente com DocumentCheck")
    void exemploComposicaoDeConferenciaEmLoteHeterogeneo() {
        List<DocumentCheck<?>> filaImportacao = Arrays.asList(
                DocumentCheck.cpf("28001238938", CpfValidationMode.NUMERIC).withFormatter(Cpf::applyMask),
                DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.ALPHANUMERIC)
                        .withPolicy(c -> "01DE".equals(c.getBranch()))
                        .withFormatter(Cnpj::applyMask),
                DocumentCheck.nis("00000000000")
                        .withPolicy(n -> n.getCanonicalValue().chars().distinct().count() > 1),
                DocumentCheck.cpf("cpf-invalido", CpfValidationMode.NUMERIC)
        );

        List<DocumentCheckResult> resultados = new ArrayList<>();
        for (DocumentCheck<?> item : filaImportacao) {
            resultados.add(item.evaluate());
        }

        assertEquals(DocumentCheckResult.Status.ACCEPTED, resultados.get(0).getStatus());
        assertEquals("280.012.389-38", resultados.get(0).getFormattedValue().orElse(""));

        assertEquals(DocumentCheckResult.Status.ACCEPTED, resultados.get(1).getStatus());
        assertEquals("12.ABC.345/01DE-35", resultados.get(1).getFormattedValue().orElse(""));

        assertEquals(DocumentCheckResult.Status.REJECTED_BY_POLICY, resultados.get(2).getStatus());
        assertFalse(resultados.get(2).getFormattedValue().isPresent());

        assertEquals(DocumentCheckResult.Status.REJECTED_BY_VALIDATION, resultados.get(3).getStatus());
    }
}
