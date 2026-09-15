package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Identifies the owners that make up a household — those sharing a last name (compared
 * case-insensitively with collapsed whitespace) and a normalized address (see
 * {@link AddressNormalizer}). Used both to guard against accidental duplicates
 * ({@link EnsureUniqueHousehold}) and to group knowing house-mates under a shared,
 * stable household id ({@link AssignHousehold}).
 */
final class Households {

    private Households() {
    }

    /** Existing owners whose last name and address match the given household. */
    static List<Owner> membersAt(OwnerRepository repository, String lastName, String address) {
        String key = key(lastName, address);
        List<Owner> members = new ArrayList<>();
        for (Owner owner : repository.findAll()) {
            if (key.equals(key(owner.getLastName(), owner.getAddress()))) {
                members.add(owner);
            }
        }
        return members;
    }

    /**
     * A stable identifier for the household with the given last name and address. Derived
     * from the normalized key, so every member of the same household resolves to the same
     * value regardless of when they are created.
     */
    static String householdId(String lastName, String address) {
        return "HH-" + sha256Hex(key(lastName, address)).substring(0, 24).toUpperCase(Locale.ROOT);
    }

    /** The normalized last name and address joined into a single household key. */
    private static String key(String lastName, String address) {
        return normalizeName(lastName) + '\n' + AddressNormalizer.normalize(address);
    }

    /** Case-fold and collapse whitespace so trivial spacing/casing differences still match. */
    private static String normalizeName(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
