package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived identity of an owner surfaced on the response: the normalized
 * telephone, the (optional) email and the household id joined by {@code '|'}. The
 * telephone and email are already canonical by the time a key is built — the create steps
 * normalize them, and stored owners hold the normalized values — so this class only joins
 * them.
 *
 * <p>Household duplicate detection itself keys directly off the {@code householdId} (see
 * {@link org.springframework.samples.petclinic.rest.function.owner.EnsureUniqueIdentity});
 * this value is exposed on the response mapper for callers.
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
