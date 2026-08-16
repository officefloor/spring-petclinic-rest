package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's identity key, the single value used for duplicate detection.
 *
 * <p>The key is {@code '<telephone>|<email>|<householdId>'} built from the owner's
 * normalized E.164 telephone, lower-cased email and household id. A {@code null}
 * email or household id contributes an empty string, so the key always has the
 * same {@code a|b|c} shape. Two owners are duplicates only when their whole keys
 * are equal.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so
 * MapStruct does not mistake it for a generic {@code String -> String} mapping
 * method and apply it to unrelated fields; the mapper references it only through
 * an explicit expression, and the create endpoint uses it for the duplicate check.
 */
public final class IdentityKeyDeriver {

    private IdentityKeyDeriver() {
    }

    /**
     * Returns the identity key {@code '<telephone>|<email>|<householdId>'}, treating
     * a {@code null} value in any component as the empty string.
     */
    public static String identityKey(String telephone, String email, String householdId) {
        return (telephone == null ? "" : telephone)
            + "|" + (email == null ? "" : email)
            + "|" + (householdId == null ? "" : householdId);
    }
}
