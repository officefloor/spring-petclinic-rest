package org.springframework.samples.petclinic.rest.validation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Computes the SHA-256 hashes behind the derived identifiers that fingerprint owner fields. Keeping
 * the hashing in one place lets the create endpoint treat "hash these owner fields" as an opaque
 * primitive - it supplies the value to hash and how much of the digest it wants, and every derived
 * identifier is produced by the same algorithm, differing only in the value it hashes and how the
 * digest is presented.
 *
 * <p>The digest is taken over the UTF-8 bytes of the input; {@link #hexPrefix(String, int)} keeps a
 * leading run of upper-case hex characters, as used by the household-scoped {@code householdId} and
 * the {@code memberId}'s {@code HASH8} segment.
 */
@Component
public class Sha256Hasher {

    /**
     * The leading {@code length} upper-case hex characters of the SHA-256 digest of the UTF-8 bytes
     * of {@code input}. Used by the derived identifiers that keep only a short hash prefix - the
     * household-scoped {@code householdId} and the {@code memberId}'s {@code HASH8} segment - so
     * each is produced by the same algorithm and differs only in the value it hashes and the number
     * of hex characters it keeps.
     *
     * @param input the value to hash
     * @param length the number of leading hex characters to keep
     * @return the leading {@code length} upper-case hex characters of the SHA-256 digest
     */
    public String hexPrefix(String input, int length) {
        return hex(input).substring(0, length).toUpperCase(Locale.ROOT);
    }

    /**
     * The full lower-case hex encoding of the SHA-256 digest of the UTF-8 bytes of {@code input}.
     * This is the shared digest computation used directly by callers that keep the whole 64-character
     * digest (such as the owner {@code identityKey}); callers that keep only a leading, upper-case run
     * of it go through {@link #hexPrefix(String, int)}.
     *
     * @param input the value to hash
     * @return the SHA-256 digest as a lower-case hex string
     */
    public String hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
