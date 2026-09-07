package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Single home for owner address normalization. The create pipeline's {@link BuildOwner}
 * (which stores the canonical form and rejects an address blank after normalization) and
 * {@link Household} (which matches household members and derives the shared household id) both
 * go through here, so the stored form and every address comparison stay defined in one place.
 */
final class AddressNormalizer {

    private AddressNormalizer() {
    }

    /** Common street-type abbreviations expanded to their canonical (upper-cased) word. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    /**
     * Canonical stored form of a supplied address: leading/trailing and repeated whitespace
     * collapsed to single spaces, upper-cased, then each whitespace-delimited word that is a
     * known abbreviation expanded ({@code ST}->{@code STREET}, {@code RD}->{@code ROAD},
     * {@code AVE}->{@code AVENUE}). So {@code "  12  main  st "} becomes {@code "12 MAIN STREET"}.
     * A {@code null} or all-whitespace address normalizes to the empty string.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] words = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(words[i], words[i]));
        }
        return sb.toString();
    }
}
