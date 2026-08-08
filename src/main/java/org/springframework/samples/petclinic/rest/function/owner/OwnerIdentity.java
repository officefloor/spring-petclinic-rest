package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the single {@code identityKey} used for all owner duplicate detection.
 *
 * <p>The key is {@code normalizedTelephone + '|' + (email or empty) + '|' +
 * (householdId or empty)}. A create is a duplicate only when a new owner's WHOLE
 * identityKey equals an existing owner's — the former separate telephone, email and
 * household checks are now expressed through this one key. Because the telephone is
 * part of the key, two members of the same household (same {@code householdId}) with
 * different telephones have different keys and are both allowed; only an exact
 * full-key match is a duplicate.
 *
 * <p>Both sides are canonicalized the same way so a new owner and the stored owners
 * compare on equal footing: telephone via {@link E164Telephone}, email trimmed and
 * lower-cased, {@code householdId} used verbatim (empty when absent).
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The owner's derived identity key. Never {@code null}. */
    public static String identityKey(Owner owner) {
        return telephone(owner) + "|" + email(owner) + "|" + householdId(owner);
    }

    private static String telephone(Owner owner) {
        String e164 = E164Telephone.toE164OrNull(owner.getTelephone());
        if (e164 != null) {
            return e164;
        }
        return owner.getTelephone() == null ? "" : owner.getTelephone();
    }

    private static String email(Owner owner) {
        String value = owner.getEmail();
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed.toLowerCase(Locale.ROOT);
    }

    private static String householdId(Owner owner) {
        return owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
    }
}
