package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived duplicate-detection key for an owner. All duplicate detection on create
 * flows through this one value: the normalized (E.164) telephone, a {@code '|'}, the email
 * (lower-cased and trimmed, or empty when absent), a {@code '|'}, and the household id (or
 * empty). Two owners are duplicates only when their WHOLE key is equal — so, because the
 * telephone is part of the key, two members of the same household (same household id) with
 * different telephones have different keys and are both allowed.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String key(Owner owner) {
        return telephone(owner.getTelephone()) + "|" + email(owner.getEmail()) + "|"
                + household(owner.getHouseholdId());
    }

    /** Normalized telephone: the E.164 form used everywhere else (see {@link TelephoneNormalizer}). */
    private static String telephone(String telephone) {
        return telephone == null ? "" : TelephoneNormalizer.comparisonKey(telephone);
    }

    /** Email lower-cased and trimmed; a null/blank email contributes the empty string. */
    private static String email(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }

    /** The owner's household id, or empty when the owner is not part of a shared household. */
    private static String household(String householdId) {
        return householdId == null ? "" : householdId.strip();
    }
}
