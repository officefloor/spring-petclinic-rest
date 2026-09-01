package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single duplicate-detection key for an owner: the SHA-256 hex digest of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. Two owners are
 * duplicates only when their whole identity keys are equal, so members of the same household
 * with different telephones (or unlike-sounding names) get distinct keys.
 */
final class IdentityKey {

    private IdentityKey() {
    }

    static String of(Owner owner) {
        String raw = digits(owner.getTelephone()) + '|' + orEmpty(owner.getEmail()).toLowerCase()
                + '|' + Soundex.of(owner.getLastName());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String digits(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
