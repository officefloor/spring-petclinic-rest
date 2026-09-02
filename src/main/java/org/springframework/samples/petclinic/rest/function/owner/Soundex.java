package org.springframework.samples.petclinic.rest.function.owner;

/**
 * American Soundex of a surname: the first letter followed by three consonant-class digits. Used as
 * the phonetic component of an owner's {@link IdentityKey} and as the soft-match key (similar-sounding
 * last names in the same postcode). Empty for a null or letter-free name.
 */
public final class Soundex {

    private Soundex() {
    }

    public static String of(String name) {
        String s = name == null ? "" : name.replaceAll("[^A-Za-z]", "").toUpperCase();
        if (s.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(s.charAt(0));
        char prev = digit(s.charAt(0));
        for (int i = 1; i < s.length() && code.length() < 4; i++) {
            char c = s.charAt(i);
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
