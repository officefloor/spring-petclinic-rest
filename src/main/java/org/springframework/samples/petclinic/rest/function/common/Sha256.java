package org.springframework.samples.petclinic.rest.function.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Shared SHA-256 hex derivation. Several owner-identity values are the hex encoding of a
 * SHA-256 digest — the full lower-case digest (the duplicate-detection identity key), the
 * first 12 upper-hex chars (the household id, see
 * {@code org.springframework.samples.petclinic.rest.function.owner.OwnerHousehold}) and the
 * first 8 upper-hex chars (the customerCode hash, see
 * {@code org.springframework.samples.petclinic.rest.function.owner.AssignOwnerCustomerCode}) —
 * so the digest itself is computed here once rather than reimplemented per call site.
 */
public final class Sha256 {

    private Sha256() {
    }

    /** Full lower-case hex of the SHA-256 digest over the UTF-8 bytes of {@code input}. */
    public static String hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The first {@code n} upper-case hex characters of the SHA-256 digest of {@code input}. */
    public static String hexPrefix(String input, int n) {
        return hex(input).substring(0, n).toUpperCase(Locale.ROOT);
    }
}
