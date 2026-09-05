package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives the single duplicate-detection key that consolidates the former separate
 * telephone, email and household checks: {@code normalizedTelephone|email|householdId},
 * with an empty segment for an absent email. Telephone is already E.164-normalised and
 * email lower-cased by earlier steps, and {@link HouseholdId} normalises lastName/address,
 * so two owners collide only when their whole key matches.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(String telephone, String email, String lastName, String address) {
        return blankToEmpty(telephone) + '|' + blankToEmpty(email) + '|'
                + HouseholdId.of(lastName, address);
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
