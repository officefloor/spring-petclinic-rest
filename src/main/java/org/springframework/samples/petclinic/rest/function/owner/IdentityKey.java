package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of an owner's <em>identity key</em>: the value that decides when
 * two owners are the same person. It joins the owner's normalized telephone, email and
 * household identifier into one string, so a duplicate is an exact match of the whole key
 * rather than of any single field.
 *
 * <p>Key = {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}.
 * All three parts are already stored in canonical form ({@link NormalizeOwnerTelephone}
 * stores the E.164 telephone, {@link NormalizeOwnerEmail} the lower-cased email and
 * {@link AssignHousehold} the shared household id), so the key is built from the owner's
 * stored fields; email is lower-cased defensively so the comparison is case-insensitive.
 *
 * <p>Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed.
 *
 * @see EnsureUniqueIdentity rejects a create request whose identity key matches an existing owner.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The identity key of an existing owner, derived from its stored fields. */
    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /** The identity key for the given normalized telephone, email and household id. */
    public static String of(String telephone, String email, String householdId) {
        return part(telephone) + "|" + lowerCased(email) + "|" + part(householdId);
    }

    /** A field value, or the empty string when absent. */
    private static String part(String value) {
        return value == null ? "" : value;
    }

    /** An email lower-cased, or the empty string when absent. */
    private static String lowerCased(String email) {
        return email == null ? "" : email.toLowerCase(Locale.ROOT);
    }
}
