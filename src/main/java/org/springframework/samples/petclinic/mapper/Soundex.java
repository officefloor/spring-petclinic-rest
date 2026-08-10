package org.springframework.samples.petclinic.mapper;

import java.util.Locale;

/**
 * American Soundex coder used to reduce a surname to a phonetic code. Kept in its own
 * class (rather than as a method on {@link OwnerMapper}) so MapStruct does not mistake a
 * {@code String -> String} helper for an implicit property mapping method.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * The American Soundex code of a name: its first letter followed by three digits
     * encoding the remaining consonants (b,f,p,v=1; c,g,j,k,q,s,x,z=2; d,t=3; l=4;
     * m,n=5; r=6), with adjacent equal codes (and codes separated only by h/w) collapsed
     * and vowels acting as separators. The result is upper-cased and padded with trailing
     * zeros to exactly four characters. A {@code null}/blank or letter-less name yields the
     * empty string.
     */
    public static String of(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // Transparent: does not emit a digit nor reset the previous code.
                continue;
            }
            char digit = codeOf(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            // Vowels ('0') reset the run so a later equal consonant is coded again.
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char codeOf(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }
}
