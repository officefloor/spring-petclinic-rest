package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Deterministic household id for an owner: the first 12 upper-case hex characters of
 * SHA-256 over {@code normalizedLastName + '|' + postcode}. Owners with the same last
 * name (compared case-insensitively with collapsed whitespace) and postcode therefore
 * share it automatically, without any stored link.
 */
final class HouseholdId {

    private HouseholdId() {
    }

    static String of(Owner owner) {
        String lastName = owner.getLastName() == null ? ""
                : owner.getLastName().trim().replaceAll("\\s+", " ").toLowerCase();
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(("V2|" + lastName + '|' + postcode).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12).toUpperCase();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
