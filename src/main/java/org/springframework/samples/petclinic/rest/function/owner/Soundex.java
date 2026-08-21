package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The American Soundex phonetic encoding of a surname, used by the owner identity key and the
 * soft duplicate match so that names that sound alike collapse to the same code.
 *
 * <p>The code is the first letter of the name followed by three digits: consonants map to
 * {@code b,f,p,v -> 1}; {@code c,g,j,k,q,s,x,z -> 2}; {@code d,t -> 3}; {@code l -> 4};
 * {@code m,n -> 5}; {@code r -> 6}. Vowels (and {@code y}) separate codes; {@code h} and
 * {@code w} do not, so two like-coded consonants around an {@code h}/{@code w} coalesce.
 * Adjacent duplicate codes collapse to one. The result is padded with zeros or truncated to
 * exactly four characters. A name with no letters encodes to the empty string.
 */
final class Soundex {

    private Soundex() {
    }

    static String encode(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = Character.toUpperCase(name.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        char first = letters.charAt(0);
        code.append(first);
        int prev = codeOf(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // does not separate like-coded consonants, and carries no code
            }
            int digit = codeOf(c);
            if (digit != 0 && digit != prev) {
                code.append(digit);
            }
            prev = (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y') ? 0 : digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static int codeOf(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V':
                return 1;
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z':
                return 2;
            case 'D': case 'T':
                return 3;
            case 'L':
                return 4;
            case 'M': case 'N':
                return 5;
            case 'R':
                return 6;
            default:
                return 0;
        }
    }
}
