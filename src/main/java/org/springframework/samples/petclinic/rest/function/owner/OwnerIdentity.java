package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Derives an owner's {@code identityKey}, the single value all duplicate detection is based on:
 * the SHA-256 hex digest of {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
 * Two owners are duplicates only when their whole keys are equal.
 *
 * <p>Telephone is part of the key, so two owners sharing a last name and postcode but carrying
 * different telephones have different keys and are not hard duplicates: they are allowed and merely
 * flagged as a possible (soft) duplicate elsewhere.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String key(String telephone, String email, String lastName) {
        String tel = telephone == null ? "" : telephone;
        String mail = email == null ? "" : email.toLowerCase();
        String raw = tel + "|" + mail + "|" + Soundex.of(lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
