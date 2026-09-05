package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives the single duplicate-detection key that consolidates the former separate
 * telephone, email and household checks: {@code normalizedTelephone|email|householdId},
 * with an empty segment for an absent email. Telephone is already E.164-normalised and
 * email lower-cased by earlier steps, and {@link HouseholdId} keys on lastName/postcode,
 * so two owners collide only when their whole key matches.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(String telephone, String email, String lastName, String postcode) {
        return blankToEmpty(telephone) + '|' + blankToEmpty(email) + '|'
                + HouseholdId.of(lastName, postcode);
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
