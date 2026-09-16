package org.springframework.samples.petclinic.model;

import java.util.Locale;

/**
 * The single definition of an owner's <em>identity key</em>: the SHA-256 fingerprint that
 * decides when two owners are the same person. It is the full 64-character lower-case hex
 * digest of {@code V2 + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)},
 * where {@code V2} is the {@link IdentityVersion#TAG} version tag, and is both returned on the
 * owner response and used by the create pipeline to reject a duplicate.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * The identity key for the given fields: the lower-case hex SHA-256 of
     * {@code V2 + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, where
     * {@code V2} is the {@link IdentityVersion#TAG} version tag. The telephone
     * is expected already normalized (E.164) by the create pipeline; the email is lower-cased
     * and the last name reduced to its {@link Soundex} code here, so callers may pass the raw
     * stored values. A null telephone or email contributes an empty string.
     */
    public static String key(String telephone, String email, String lastName) {
        String raw = IdentityVersion.TAG + '|' + part(telephone) + '|' + lower(email) + '|'
                + Soundex.encode(lastName);
        return Sha256.lowerHex(raw);
    }

    /** The identity key of a stored owner, derived from its telephone, email and last name. */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    private static String part(String value) {
        return value == null ? "" : value;
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
