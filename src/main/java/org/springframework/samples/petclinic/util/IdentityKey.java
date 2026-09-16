package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived identity of an owner used for duplicate detection: the normalized
 * telephone, the (optional) email and the household id joined by {@code '|'}. Two owners
 * are duplicates exactly when their whole identity keys are equal, so the previously
 * separate telephone, email and household checks are all expressed through this one value.
 *
 * <p>Both the create pipeline (which compares a candidate's key against every existing
 * owner) and the response mapper (which returns the key) derive it here, so they agree on
 * what an owner's identity is. The telephone and email are already canonical by the time a
 * key is built — the create steps normalize them before the check, and stored owners hold
 * the normalized values — so this class only joins them.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The identity key for the given owner, from its stored telephone, email and household id. */
    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /**
     * The identity key for the given parts: {@code telephone + '|' + (email or empty) + '|'
     * + (householdId or empty)}. A null or blank email or household id contributes an empty
     * segment, so an owner without one still has a well-formed key.
     */
    public static String of(String telephone, String email, String householdId) {
        return orEmpty(telephone) + '|' + orEmpty(email) + '|' + orEmpty(householdId);
    }

    private static String orEmpty(String value) {
        return (value == null || value.isBlank()) ? "" : value;
    }
}
