package org.springframework.samples.petclinic.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SoundexTests {

    @Test
    void codesStandardExamples() {
        assertThat(Soundex.of("Robert")).isEqualTo("R163");
        assertThat(Soundex.of("Rupert")).isEqualTo("R163");
        assertThat(Soundex.of("Rubin")).isEqualTo("R150");
        assertThat(Soundex.of("Tymczak")).isEqualTo("T522");
        assertThat(Soundex.of("Pfister")).isEqualTo("P236");
        assertThat(Soundex.of("Honeyman")).isEqualTo("H555");
    }

    @Test
    void lettersSeparatedByHOrWAreCodedOnce() {
        assertThat(Soundex.of("Ashcraft")).isEqualTo("A261");
        assertThat(Soundex.of("Ashcroft")).isEqualTo("A261");
    }

    @Test
    void ignoresCaseAndNonLetters() {
        assertThat(Soundex.of("o'brien")).isEqualTo(Soundex.of("OBrien"));
        assertThat(Soundex.of("Smith")).isEqualTo(Soundex.of("Smyth"));
    }

    @Test
    void emptyWithoutLetters() {
        assertThat(Soundex.of(null)).isEmpty();
        assertThat(Soundex.of("")).isEmpty();
        assertThat(Soundex.of("123")).isEmpty();
    }
}
