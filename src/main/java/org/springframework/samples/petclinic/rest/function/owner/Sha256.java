package org.springframework.samples.petclinic.rest.function.owner;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Computes the SHA-256 hex digest of a string: its UTF-8 bytes hashed with SHA-256 and
 * rendered as 64 lower-case hex characters. Several owner derivations key off a stable
 * hash of some composed string (the {@code householdId} is the leading 12 characters of
 * such a digest, see {@link AssignHousehold}); centralising the digest here keeps that one
 * primitive in one place. It delegates to Apache Commons Codec's {@link DigestUtils} rather
 * than re-implementing the {@link java.security.MessageDigest} boilerplate.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without
 * tripping the one-public-method-per-function rule.
 */
public final class Sha256 {

    private Sha256() {
    }

    /**
     * @param input the string to hash; its UTF-8 bytes are digested.
     * @return the SHA-256 digest as 64 lower-case hex characters.
     */
    public static String hex(String input) {
        return DigestUtils.sha256Hex(input);
    }
}
