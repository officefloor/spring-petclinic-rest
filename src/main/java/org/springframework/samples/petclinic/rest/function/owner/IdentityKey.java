package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's identity key — the single value all duplicate detection is based on.
 * It is the SHA-256 hex digest over the normalized telephone, the lower-cased email and the
 * {@link Soundex} of the last name, joined by '|'. Two owners are duplicates only when their
 * whole keys match; a shared telephone, email or surname alone is not enough.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail().toLowerCase(Locale.ROOT);
        String seed = "V2|" + telephone + "|" + email + "|" + Soundex.of(owner.getLastName());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
