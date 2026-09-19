package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Shared telephone normalization: strips every non-digit character, leaving the bare
 * digit sequence. Used both to canonicalize an incoming request's telephone and to
 * compare it against the telephones already stored on other owners.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    static String digits(String raw) {
        return raw == null ? "" : raw.replaceAll("\\D", "");
    }
}
