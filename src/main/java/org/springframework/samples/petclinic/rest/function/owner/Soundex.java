package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * American Soundex of a name: the first letter followed by three digits encoding the remaining
 * consonants (vowels drop out, letters sharing a code collapse, {@code h}/{@code w} are transparent).
 * Two surnames that sound alike share a code, which is how the identity key and the soft-match rule
 * group households phonetically rather than on exact spelling.
 */
public final class Soundex {

    private Soundex() {
    }

    public static String of(String value) {
        String letters = value == null ? "" : value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char prev = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != prev) {
                code.append(d);
            }
            if (c != 'H' && c != 'W') {
                prev = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char digit(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V': return '1';
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z': return '2';
            case 'D': case 'T': return '3';
            case 'L': return '4';
            case 'M': case 'N': return '5';
            case 'R': return '6';
            default: return '0';
        }
    }
}
