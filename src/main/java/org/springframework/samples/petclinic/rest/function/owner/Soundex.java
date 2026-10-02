package org.springframework.samples.petclinic.rest.function.owner;

/**
 * American Soundex encoding of a last name, shared by the owner-create pipeline. Soundex reduces a
 * name to a letter-plus-three-digit code that groups similarly sounding names (e.g. {@code Robert}
 * and {@code Rupert} both encode to {@code R163}), so it is used as the name segment of the owner
 * {@link OwnerIdentity identity key} and as the surname match in the soft-match rule
 * ({@link PossibleDuplicate}).
 *
 * <p>The algorithm: keep only letters, retain the first letter, map the rest to digits
 * ({@code B,F,P,V}->1; {@code C,G,J,K,Q,S,X,Z}->2; {@code D,T}->3; {@code L}->4; {@code M,N}->5;
 * {@code R}->6; vowels and {@code H,W,Y}->0), collapse adjacent equal digits (and equal digits
 * separated only by {@code H} or {@code W}), drop the zero codes, then pad or truncate to a letter
 * followed by three digits. A {@code null} or letterless input yields {@code ""}.
 */
final class Soundex {

    private Soundex() {
    }

    /** The Soundex code of {@code name} (e.g. {@code "R163"}), or {@code ""} when it has no letters. */
    static String encode(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        char first = letters.charAt(0);
        code.append(first);
        char prev = codeOf(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = codeOf(c);
            if (d != '0' && d != prev) {
                code.append(d);
            }
            // H and W do not separate two equal codes; every other letter (including vowels and Y)
            // resets the previous code so a repeat after it is coded again.
            if (c != 'H' && c != 'W') {
                prev = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char codeOf(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V':
                return '1';
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z':
                return '2';
            case 'D': case 'T':
                return '3';
            case 'L':
                return '4';
            case 'M': case 'N':
                return '5';
            case 'R':
                return '6';
            default:
                return '0';
        }
    }
}
