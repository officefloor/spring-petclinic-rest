package org.springframework.samples.petclinic.mapper;

/**
 * American Soundex code of a surname: the first letter followed by three digits derived
 * from the remaining consonants, padded with zeros (H and W are transparent, vowels
 * reset adjacency). Used to group phonetically similar last names for identity-key and
 * soft-duplicate matching.
 */
public final class Soundex {

    private static final String CODES = "01230120022455012623010202";

    private Soundex() {
    }

    public static String of(String value) {
        String s = value == null ? "" : value.toUpperCase().replaceAll("[^A-Z]", "");
        if (s.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder().append(s.charAt(0));
        char prev = CODES.charAt(s.charAt(0) - 'A');
        for (int i = 1; i < s.length() && out.length() < 4; i++) {
            char code = CODES.charAt(s.charAt(i) - 'A');
            if (code != '0' && code != prev) {
                out.append(code);
            }
            if (s.charAt(i) != 'H' && s.charAt(i) != 'W') {
                prev = code;
            }
        }
        while (out.length() < 4) {
            out.append('0');
        }
        return out.toString();
    }
}
