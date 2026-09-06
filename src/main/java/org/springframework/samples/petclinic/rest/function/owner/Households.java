package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the deterministic {@code householdId}: the first 12 hex characters of
 * {@code SHA-256(normalizedLastName + '|' + postcode)}.
 *
 * <p>Because it depends only on the last name and postcode, every owner sharing those two values
 * resolves to the same id automatically, without any request opting in and without back-filling
 * other members. The last name is normalized case-insensitively with runs of whitespace collapsed;
 * the postcode is trimmed and contributes an empty segment when absent.
 */
public final class Households {

    private Households() {
    }

    /** The householdId for the owner, derived from its last name and postcode. */
    public static String id(Owner owner) {
        return id(owner.getLastName(), owner.getPostcode());
    }

    /** The householdId for the given last name and postcode. */
    public static String id(String lastName, String postcode) {
        String input = normalize(lastName) + '|' + segment(postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String segment(String value) {
        return value == null ? "" : value.trim();
    }
}
