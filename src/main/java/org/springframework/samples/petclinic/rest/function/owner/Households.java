package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Identifies the owners that make up a household — those sharing a last name (compared
 * case-insensitively with collapsed whitespace) and a normalized address (see
 * {@link AddressNormalizer}). Used both to derive an owner's identity key
 * ({@link EnsureUniqueIdentity}) and to group knowing house-mates under a shared, stable
 * household id ({@link AssignHousehold}).
 */
final class Households {

    private Households() {
    }

    /**
     * The household id a create request would resolve to: the shared id for its last name
     * and address when it opts in with {@code sharesHousehold=true} and existing owners are
     * already at that household, otherwise null. Mirrors what {@link AssignHousehold} would
     * assign, so the identity check compares the candidate against the same id.
     */
    static String resolveHouseholdId(OwnerRepository repository, OwnerFieldsDto request) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return null;
        }
        if (membersAt(repository, request.getLastName(), request.getAddress()).isEmpty()) {
            return null;
        }
        return householdId(request.getLastName(), request.getAddress());
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

    /** How many existing owners already carry the given household id (0 when it is null). */
    static int memberCount(OwnerRepository repository, String householdId) {
        if (householdId == null) {
            return 0;
        }
        int count = 0;
        for (Owner owner : repository.findAll()) {
            if (householdId.equals(owner.getHouseholdId())) {
                count++;
            }
        }
        return count;
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
