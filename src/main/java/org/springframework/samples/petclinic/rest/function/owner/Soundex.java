package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * American Soundex code of a name: its first letter followed by three digits derived from the
 * remaining consonants (like-sounding letters share a digit; vowels and repeats drop out, {@code H}
 * and {@code W} never separate), padded with zeros to four characters. A null or letterless name
 * yields {@code "0000"}. Used both inside the {@link IdentityKey} and by {@link PossibleDuplicate},
 * so last names that merely sound alike are grouped identically in each.
 */
public final class Soundex {

    private static final String DIGITS = "01230120022455012623010202";

    private Soundex() {
    }

    public static String of(String name) {
        String letters = name == null ? "" : name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "0000";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != previous) {
                code.append(d);
            }
            if (c != 'H' && c != 'W') {
                previous = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char digit(char c) {
        return DIGITS.charAt(c - 'A');
    }
}
