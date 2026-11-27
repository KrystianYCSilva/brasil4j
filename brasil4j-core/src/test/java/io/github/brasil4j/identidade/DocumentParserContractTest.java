package io.github.brasil4j.identidade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contrato comum a todo {@link DocumentParser}: nunca lança exceção para entrada inválida, classifica a
 * rejeição, aplica regras só após a aceitação nativa e não expõe a entrada em diagnósticos.
 *
 * @param <D> documento interpretado
 */
public abstract class DocumentParserContractTest<D extends Document> {
    /** @return interpretador sob teste */
    protected abstract DocumentParser<D> parser();

    /** @return entrada aceita pelo interpretador */
    protected abstract String validInput();

    /** @return mensagem esperada em {@code parseOrThrow} */
    protected abstract String errorMessage();

    @ParameterizedTest
    @NullAndEmptySource
    void ausenciaEhRejeicaoOrdinaria(String entrada) {
        assertEquals(Optional.of(Rejection.of(RejectionReason.MISSING)), parser().parse(entrada).getRejection());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "lixo", "\t", "1", "!@#$%", "０１２３"})
    void formatoInvalidoEhRejeicaoOrdinaria(String entrada) {
        ValidationResult<D> resultado = parser().parse(entrada);
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)), resultado.getRejection());
        assertFalse(parser().isValid(entrada));
        assertFalse(resultado.toString().contains(entrada));
    }

    @Test
    void aceitaAEntradaValida() {
        ValidationResult<D> resultado = parser().parse(validInput());
        assertTrue(resultado.isValid());
        assertTrue(parser().isValid(validInput()));
        assertEquals(resultado.getDocument().get(), parser().parseOrThrow(validInput()));
        assertFalse(resultado.toString().contains(validInput()));
    }

    @Test
    void interpretacaoEstritaLancaErroExatoSemAEntrada() {
        IllegalArgumentException erro = assertThrowsExactly(IllegalArgumentException.class,
                () -> parser().parseOrThrow("lixo-123"));
        assertEquals(errorMessage(), erro.getMessage());
        assertThrowsExactly(IllegalArgumentException.class, () -> parser().parseOrThrow(null));
    }

    @Test
    void politicaRodaSomenteDepoisDaAceitacaoNativa() {
        List<String> chamadas = new ArrayList<>();
        DocumentParser<D> comPolitica = parser().withPolicy(documento -> {
            chamadas.add("politica");
            return false;
        }, "TESTE");

        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_FORMAT)),
                comPolitica.parse("lixo").getRejection());
        assertTrue(chamadas.isEmpty());
        assertEquals(Optional.of(Rejection.of(RejectionReason.POLICY, "TESTE")),
                comPolitica.parse(validInput()).getRejection());
        assertEquals(Arrays.asList("politica"), chamadas);
        assertEquals(errorMessage(), assertThrowsExactly(IllegalArgumentException.class,
                () -> comPolitica.parseOrThrow(validInput())).getMessage());
    }

    @Test
    void regrasDecoradasEncadeiamEmOrdem() {
        List<String> chamadas = new ArrayList<>();
        DocumentParser<D> decorado = parser()
                .withRule(documento -> {
                    chamadas.add("primeira");
                    return Optional.empty();
                })
                .withPolicy(documento -> {
                    chamadas.add("segunda");
                    return true;
                });
        assertTrue(decorado.isValid(validInput()));
        assertEquals(Arrays.asList("primeira", "segunda"), chamadas);
        assertEquals(Optional.of(Rejection.of(RejectionReason.POLICY)),
                parser().withPolicy(documento -> false).parse(validInput()).getRejection());
    }

    @Test
    void decoracaoRecusaNulos() {
        assertThrows(NullPointerException.class, () -> parser().withRule(null));
        assertThrows(NullPointerException.class, () -> parser().withPolicy(null));
        assertThrows(NullPointerException.class, () -> parser().withPolicy(null, "A"));
        assertThrows(NullPointerException.class, () -> parser().withPolicy(documento -> true, null));
    }
}
