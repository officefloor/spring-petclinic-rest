package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * The single derived identity of a pet owner, consolidating the former telephone, email and
 * household duplicate checks into one derived key. The {@code identityKey} is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the telephone is
 * canonical E.164 form, the email is trimmed and lower-cased (or empty when absent) and the household
 * id is {@link Household#id(String, String)} over the last name and address.
 *
 * <p>Because the telephone is part of the key, two members of the same household (same householdId)
 * with different telephones have different identity keys and are both allowed; only owners that are
 * the same contact collide. Duplicate detection compares the {@link #contactKey(String, String)} —
 * the telephone-and-email prefix of the key that is what actually identifies one owner, so two owners
 * in the same household with different telephones (or emails) are distinct and both persist.
 *
 * <p>Used by {@link RequireUniqueIdentity} to reject a collision and by the owner mapper to expose
 * the full key on responses.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The full identity key for an owner with the given raw fields, as exposed on responses. Fields
     *  are normalized here so the same value is produced whether they come from a freshly-validated
     *  request or a stored owner. */
    public static String key(String telephone, String email, String lastName, String address) {
        return contactKey(telephone, email) + "|" + Household.id(lastName, address);
    }

    /** The telephone-and-email prefix of the identity key, identifying a single contact. Two owners
     *  are duplicates when their contact keys are equal. */
    public static String contactKey(String telephone, String email) {
        return normalizedTelephone(telephone) + "|" + normalizeEmail(email);
    }

    /** The telephone in canonical E.164 form, or the raw value (or empty when {@code null}) when it
     *  cannot be normalized. Shared so the customer-code hash and the identity key normalize the
     *  telephone the same way. */
    public static String normalizedTelephone(String telephone) {
        try {
            return OwnerTelephone.toE164(telephone);
        }
        catch (InvalidTelephoneException ex) {
            return telephone == null ? "" : telephone;
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
