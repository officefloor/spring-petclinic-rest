package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Derives the stable {@code householdId} shared by owners who live together at the same
 * address. The id is the {@link #hash(String) SHA-256 prefix} of the normalized last name
 * and address, so every owner sharing a household (same normalized last name + address)
 * maps to the same value.
 *
 * <p>Centralising the derivation here keeps {@link AssignHousehold} (which assigns the id)
 * and {@link IdentityKeys} (which folds it into an owner's identity key) in exact agreement.
 */
public final class Households {

    private Households() {
    }

    /** The stable {@code householdId} for the given last name and address. */
    public static String idFor(String lastName, String address) {
        return hash(normalize(lastName) + "|" + AddressNormalizer.normalize(address));
    }

    /** Lower-case, trim, and collapse runs of whitespace to a single space. */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** Stable 16-hex-character (upper-case) prefix of the SHA-256 of the household key. */
    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
