package org.springframework.samples.petclinic.mapper;

/**
 * Derives the single duplicate-detection key for an owner:
 * {@code normalizedTelephone + '|' + (email or empty)}. Two owners are duplicates only
 * when their whole identity keys match. Because the telephone is part of the key, two
 * members of one household with different telephones get different keys and are both
 * allowed; only an exact full-key match is a duplicate.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(String telephone, String email) {
        return telephone + "|" + (email == null ? "" : email);
    }
}
