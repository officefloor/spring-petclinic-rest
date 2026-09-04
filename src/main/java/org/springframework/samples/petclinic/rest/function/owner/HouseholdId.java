package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's deterministic {@code householdId}: the first 12 hex characters of SHA-256 over
 * (normalized last name + '|' + postcode). Two owners with the same last name and postcode share
 * the value automatically, so the household is keyed on (lastName, postcode) with no stored link.
 */
public final class HouseholdId {

    private HouseholdId() {
    }

    public static String of(Owner owner) {
        return of(owner.getLastName(), owner.getPostcode());
    }

    public static String of(String lastName, String postcode) {
        String normalized = lastName == null ? "" : lastName.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return sha256hex("V2|" + normalized + "|" + (postcode == null ? "" : postcode)).substring(0, 12);
    }

    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
