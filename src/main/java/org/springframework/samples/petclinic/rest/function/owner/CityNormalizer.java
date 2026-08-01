package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalisation for owner city names. A freshly supplied city is stored title-cased (the first
 * letter of each word upper-cased, the rest lower-cased) so a city such as {@code "new york"} is
 * kept as {@code "New York"}. Existing whitespace is preserved untouched.
 */
final class CityNormalizer {

    private CityNormalizer() {
    }

    /**
     * Title-cased form of a city name: the first letter of each whitespace-separated word is
     * upper-cased and the remaining letters lower-cased. Returns {@code null} for a {@code null}
     * input.
     */
    static String titleCase(String city) {
        if (city == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(city.length());
        boolean startOfWord = true;
        for (int i = 0; i < city.length(); i++) {
            char c = city.charAt(i);
            if (Character.isWhitespace(c)) {
                startOfWord = true;
                sb.append(c);
            } else {
                sb.append(startOfWord ? Character.toUpperCase(c) : Character.toLowerCase(c));
                startOfWord = false;
            }
        }
        return sb.toString();
    }
}
