package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.util.OwnerIdentityVersion;
import org.springframework.samples.petclinic.util.Sha256;
import org.springframework.samples.petclinic.util.Soundex;

/**
 * The derived duplicate-detection identity of an owner: a single SHA-256 key that consolidates the
 * telephone, email and phonetic last name the create pipeline rejects duplicates by. The key is the
 * SHA-256 hex digest of {@code 'V2' + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)},
 * the leading version tag distinguishing it from the version-1 key.
 *
 * <p>Two owners are duplicates only when their <em>whole</em> keys are equal. Because the telephone
 * is part of the key, two owners with the same last name and postcode but different telephones have
 * different keys and are both allowed — only an exact full-key match is a duplicate.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * The identity key for the given canonical components. The telephone is folded to its
     * {@link OwnerTelephone#toE164(String) E.164} form, the email to its
     * {@link OwnerEmail#normalize(String) lower-cased} form and the last name to its
     * {@link Soundex#encode(String) Soundex} code; an absent email or last name contributes the
     * empty string. The fixed {@link OwnerIdentityVersion#TAG version tag} is mixed in as the leading
     * component so a version-2 key can never repeat a version-1 one. The components are joined by
     * {@code '|'} and SHA-256 hex digested.
     */
    public static String key(String telephone, String email, String lastName) {
        String raw = OwnerIdentityVersion.TAG + "|" + normalizedTelephone(telephone) + "|"
                + normalizedEmail(email) + "|" + Soundex.encode(lastName);
        return Sha256.hex(raw);
    }

    private static String normalizedTelephone(String telephone) {
        return OwnerTelephone.toE164(telephone).orElse(orEmpty(telephone));
    }

    private static String normalizedEmail(String email) {
        return (email == null || email.isBlank()) ? "" : OwnerEmail.normalize(email);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
