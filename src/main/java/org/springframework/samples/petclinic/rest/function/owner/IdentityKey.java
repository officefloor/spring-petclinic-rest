package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Derives an owner's {@code identityKey}, the single value all duplicate detection is expressed
 * through:
 *
 * <pre>identityKey = normalizedTelephone + '|' + (email or empty) + '|' + householdId</pre>
 *
 * <p>The telephone is normalized to its E.164 form (see {@link E164Telephone}); a value that cannot
 * form a valid E.164 number is used verbatim so the key stays defined for every stored owner. The
 * email contributes its lower-cased form, or the empty string when absent. The householdId
 * contributes its stored value, or the empty string when the owner is not part of a household.
 *
 * <p>Two owners are duplicates only when their WHOLE identityKey is equal, so members of the same
 * household with different telephones have different keys and are both allowed.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(Owner owner) {
        return normalizeTelephone(owner.getTelephone()) + "|" + normalizeEmail(owner.getEmail()) + "|"
                + (owner.getHouseholdId() == null ? "" : owner.getHouseholdId());
    }

    private static String normalizeTelephone(String telephone) {
        try {
            return E164Telephone.normalize(telephone);
        }
        catch (InvalidTelephoneException ex) {
            return telephone == null ? "" : telephone;
        }
    }

    private static String normalizeEmail(String email) {
        return email == null || email.isBlank() ? "" : email.toLowerCase();
    }
}
