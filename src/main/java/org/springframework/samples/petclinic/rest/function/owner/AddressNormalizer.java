package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Canonical address normalization applied whenever an owner is created. The create pipeline rewrites
 * the request's address to this form before it is stored and returned, and the shared
 * {@code householdId} ({@link AssignHouseholdId}, via {@link OwnerIdentity}) — the household
 * component of the derived {@code identityKey} used for duplicate detection — derives from it.
 *
 * <p>Trims and collapses runs of whitespace to a single space, upper-cases, and expands common
 * abbreviations token by token: {@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}. So
 * {@code "  12  main  st "} becomes {@code "12 MAIN STREET"}. The transformation is idempotent, so
 * re-normalizing an already-normalized address leaves it unchanged.
 */
final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /** Returns the normalized address, or {@code ""} when {@code value} is null or blank. */
    static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String[] tokens = value.trim().toUpperCase().split("\\s+");
        StringBuilder sb = new StringBuilder(value.length());
        for (String token : tokens) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return sb.toString();
    }
}
