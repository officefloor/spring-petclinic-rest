package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * The single derived identity of a pet owner. The {@code identityKey} is the lower-case hex SHA-256
 * digest over {@code TAG + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)},
 * where {@code TAG} is the fixed {@link IdentityVersion#TAG version-2 tag}, the telephone is canonical
 * E.164 form, the email is trimmed and lower-cased (or empty when absent) and the surname is reduced
 * to its {@link Soundex} code.
 *
 * <p>Because the telephone is part of the key, two owners in the same household (same surname and
 * postcode) with different telephones have different identity keys and are both allowed; only owners
 * that are the same contact — same telephone, email and surname phonetic — collide. Duplicate
 * detection compares this {@link #key(String, String, String) identity key} directly, and
 * {@link FlagPossibleDuplicate} treats a near-match (same soundex(lastName) and postcode but a
 * different key) as a possible duplicate.
 *
 * <p>Used by {@link RequireUniqueIdentity} to reject a collision and by the owner mapper to expose
 * the key on responses.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The full identity key for an owner with the given raw fields, as exposed on responses. Fields
     *  are normalized here so the same value is produced whether they come from a freshly-validated
     *  request or a stored owner. */
    public static String key(String telephone, String email, String lastName) {
        String raw = IdentityVersion.TAG + "|" + normalizedTelephone(telephone) + "|"
                + normalizeEmail(email) + "|" + Soundex.of(lastName);
        return Sha256.hex(raw);
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
