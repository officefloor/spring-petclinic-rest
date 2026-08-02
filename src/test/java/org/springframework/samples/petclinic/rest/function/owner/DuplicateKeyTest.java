package org.springframework.samples.petclinic.rest.function.owner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Verifies duplicate-detection normalisation ignores letter case and surrounding
 * or repeated whitespace.
 */
class DuplicateKeyTest {

    @Test
    void caseAndWhitespaceAreIgnoredForTheSamePerson() {
        assertThat(DuplicateKey.of("  john   smith ")).isEqualTo(DuplicateKey.of("John Smith"));
    }

    @Test
    void differentValuesStayDistinct() {
        assertThat(DuplicateKey.of("john smith")).isNotEqualTo(DuplicateKey.of("jane smith"));
    }

    @Test
    void mixedCaseEmailsMatch() {
        assertThat(DuplicateKey.of("Owner@Example.Test")).isEqualTo(DuplicateKey.of("owner@example.test"));
    }

    @Test
    void nullAndBlankNormaliseToNull() {
        assertThat(DuplicateKey.of(null)).isNull();
        assertThat(DuplicateKey.of("   ")).isNull();
    }
}
