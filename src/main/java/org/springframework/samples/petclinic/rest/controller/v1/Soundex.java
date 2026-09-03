package org.springframework.samples.petclinic.rest.controller.v1;

/**
 * American Soundex phonetic code of a surname: the first letter followed by up to three digits.
 * Used by identity and soft-duplicate detection so that similar-sounding last names group together.
 */
final class Soundex {

    /** Digit for each letter A..Z ('0' = not coded). Indexed by {@code letter - 'A'}. */
    private static final String MAP = "01230120022455012623010202";

    private Soundex() {
    }

    /** American Soundex code (letter + three digits) of {@code name}; {@code "0000"} when blank. */
    static String code(String name) {
        String s = name == null ? "" : name.toUpperCase().replaceAll("[^A-Z]", "");
        if (s.isEmpty()) {
            return "0000";
        }
        StringBuilder sb = new StringBuilder().append(s.charAt(0));
        char prev = MAP.charAt(s.charAt(0) - 'A');
        for (int i = 1; i < s.length() && sb.length() < 4; i++) {
            char c = s.charAt(i);
            char d = MAP.charAt(c - 'A');
            if (d != '0' && d != prev) {
                sb.append(d);
            }
            if (c != 'H' && c != 'W') {
                prev = d;
            }
        }
        while (sb.length() < 4) {
            sb.append('0');
        }
        return sb.toString();
    }
}
