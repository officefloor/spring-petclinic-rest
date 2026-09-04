package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's {@code householdId}: a stable identifier shared by every owner
 * in the same household, i.e. owners with the same last name and postcode. Derived
 * deterministically, so owners with the same last name and postcode always resolve
 * to the same value.
 */
public final class Household {

    private Household() {
    }

    /** The 12 upper-case hex chars of SHA-256 over {@code normalizedLastName + '|' + postcode}. */
    public static String idFor(Owner owner) {
        String key = normalize(owner.getLastName()) + "|" + (owner.getPostcode() == null ? "" : owner.getPostcode());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
