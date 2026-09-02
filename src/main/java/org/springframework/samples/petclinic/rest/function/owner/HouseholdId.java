package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the stable identifier of an owner's household: the first 12 hex characters of SHA-256 over
 * {@code normalizedLastName + '|' + postcode} (last name compared case-insensitively with collapsed
 * whitespace, exactly as {@link RejectDuplicateOwnerHousehold} groups them). Every owner sharing a
 * last name and postcode therefore maps to the same value without any stored state, so a household
 * is keyed on {@code (lastName, postcode)} alone.
 */
public final class HouseholdId {

    private HouseholdId() {
    }

    public static String of(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        String key = "V2|" + normalize(owner.getLastName()) + '|' + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder id = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                id.append(String.format("%02X", digest[i]));
            }
            return id.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
