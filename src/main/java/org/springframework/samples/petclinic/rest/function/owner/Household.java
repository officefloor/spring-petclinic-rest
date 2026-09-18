package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Household identity shared by the create-owner steps. A household is the set of owners with the same
 * last name and address, compared case-insensitively with collapsed whitespace. Used by
 * {@link EnsureUniqueHousehold} to reject an un-opted-in duplicate and by {@link AssignHousehold} to
 * give members a single shared {@code householdId}.
 */
final class Household {

    private Household() {
    }

    /** Case-insensitive form with leading/trailing and repeated inner whitespace collapsed to one space. */
    static String canonical(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Whether {@code owner} belongs to the household identified by the given last name and address. */
    static boolean matches(Owner owner, String lastName, String address) {
        return canonical(owner.getLastName()).equals(canonical(lastName))
                && canonical(owner.getAddress()).equals(canonical(address));
    }

    /** A stable identifier for the household at the given last name and address, of the form {@code H-<hex>}. */
    static String idFor(String lastName, String address) {
        byte[] digest = sha256(canonical(lastName) + '|' + canonical(address));
        StringBuilder id = new StringBuilder("H-");
        for (int i = 0; i < 6; i++) {
            id.append(String.format("%02X", digest[i]));
        }
        return id.toString();
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
