package org.springframework.samples.petclinic.rest.function.owner;

/**
 * American Soundex code for a name: the first letter followed by three digits derived from
 * the remaining consonants, so names that sound alike share a code (e.g. {@code Robert} and
 * {@code Rupert} both give {@code R163}). Non-letters are ignored; an empty or letter-less
 * name yields the empty string.
 */
final class Soundex {

    private Soundex() {
    }

    static String of(String name) {
        String letters = name == null ? "" : name.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // bridges consonants: previous code is preserved
            }
            char d = digit(c);
            if (d != '0' && d != previous) {
                code.append(d);
            }
            previous = isVowel(c) ? '0' : d; // a vowel lets a repeated code count again
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static boolean isVowel(char c) {
        return "AEIOUY".indexOf(c) >= 0;
    }

    private static char digit(char c) {
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
