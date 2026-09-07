package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived duplicate-detection key for an owner. All duplicate detection on create flows
 * through this one value: the full lower-case SHA-256 hex over
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)} — the normalized (E.164)
 * telephone, a {@code '|'}, the email (lower-cased and trimmed, or empty when absent), a {@code '|'},
 * and the {@link Soundex Soundex} encoding of the last name. Two owners are duplicates only when
 * this whole key is equal — so, because the telephone is part of the key, two owners who share a last
 * name (and even a postcode) but have different telephones have different keys and are both allowed
 * (surfaced instead as a soft match). The household id is no longer part of the key.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String key(Owner owner) {
        String canonical = telephone(owner.getTelephone()) + "|" + email(owner.getEmail()) + "|"
                + Soundex.of(owner.getLastName());
        return ShaHex.lowerHex(canonical);
    }

    /** Normalized telephone: the E.164 form used everywhere else (see {@link TelephoneNormalizer}). */
    private static String telephone(String telephone) {
        return telephone == null ? "" : TelephoneNormalizer.comparisonKey(telephone);
    }

    /** Email lower-cased and trimmed; a null/blank email contributes the empty string. */
    private static String email(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
