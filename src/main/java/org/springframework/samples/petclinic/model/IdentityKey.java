package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's {@code identityKey}, the single value all duplicate detection is
 * expressed through and which is also exposed on the owner response.
 *
 * <p>The key is the lower-case hex SHA-256 digest over
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. By the time an
 * owner is built its telephone has been normalized to E.164 and its email lower-cased, so
 * those stored values are the normalized forms; a null email contributes an empty segment.
 * The last-name segment is its {@link Soundex} code (never null).
 *
 * <p>Duplicate detection (409) is keyed on this whole value: a create is rejected only when
 * its {@code identityKey} equals an existing non-deleted owner's (see the create pipeline's
 * identity block). Because the telephone is part of the key, two owners sharing only a last
 * name and postcode are no longer a hard duplicate — they surface as a soft match instead.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The identity key for the given owner. */
    public static String of(Owner owner) {
        String key = segment(owner.getTelephone()) + "|" + segment(owner.getEmail()) + "|"
                + Soundex.of(owner.getLastName());
        return Sha256.hex(key);
    }

    private static String segment(String value) {
        return value == null ? "" : value;
    }
}
