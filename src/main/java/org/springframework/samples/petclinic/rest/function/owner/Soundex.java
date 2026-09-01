package org.springframework.samples.petclinic.rest.function.owner;

/**
 * American Soundex of a surname: the first letter followed by three digits encoding the remaining
 * consonants, so phonetically similar last names share a code. Used both by the owner
 * {@link OwnerIdentity#key(org.springframework.samples.petclinic.model.Owner) identityKey} and by the
 * {@link FlagPossibleDuplicate} soft-match.
 */
public final class Soundex {

    private static final String CODES = "01230120022455012623010202";

    private Soundex() {
    }

    /** Soundex code (one letter + three digits) of {@code name}; "" when it has no letters. */
    public static String encode(String name) {
        String letters = name == null ? "" : name.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char prev = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = codeOf(letters.charAt(i));
            if (c != '0' && c != prev) {
                code.append(c);
            }
            if (letters.charAt(i) != 'H' && letters.charAt(i) != 'W') {
                prev = c;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char codeOf(char letter) {
        return CODES.charAt(letter - 'A');
    }
}
