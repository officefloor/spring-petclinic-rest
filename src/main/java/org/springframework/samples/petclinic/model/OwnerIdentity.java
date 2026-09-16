package org.springframework.samples.petclinic.model;

/**
 * The single definition of an owner's <em>identity key</em>: a derived, human-readable
 * fingerprint returned on the owner response. It joins the normalized telephone, the email
 * (or empty) and the household id (or empty) with {@code '|'}.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * The identity key {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId
     * or empty)}. The telephone and email are expected already normalized (E.164 / lower-cased)
     * by the create pipeline; a null part contributes an empty string.
     */
    public static String key(String telephone, String email, String householdId) {
        return part(telephone) + '|' + part(email) + '|' + part(householdId);
    }

    /** The identity key of a stored owner, derived from its telephone, email and household id. */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    private static String part(String value) {
        return value == null ? "" : value;
    }
}
