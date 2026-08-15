package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Shared household logic for the create-owner pipeline. Owners belong to the same household when
 * they share a last name and address (the last name compared case-insensitively with collapsed
 * whitespace and the address by its normalized form, see {@link OwnerAddress}). The household id is
 * a stable 16-char upper-hex value derived deterministically from that normalized pair.
 *
 * <p>Used both by {@link AssignHousehold}, which persists the id, and by {@link OwnerIdentityKey},
 * which needs the id a request <em>would</em> be assigned so an owner's identity key is computed
 * from the same value before and after it is saved.
 */
final class Household {

    private Household() {
    }

    /**
     * Returns the household id the given create-owner request would be assigned, or {@code null}
     * when it opts out of a shared household or joins no existing household. Read-only.
     */
    static String resolveId(OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (!optIn(request)) {
            return null;
        }
        String lastName = normalizeName(request.getLastName());
        String address = OwnerAddress.normalize(request.getAddress());
        return idFor(lastName, address, membersOf(lastName, address, ownerRepository));
    }

    /**
     * Assigns the shared household id to {@code owner} (and writes it back onto the existing
     * same-household owners) when the request opts in and there is an existing same-household
     * owner. Mutates in place; leaves the id unset otherwise.
     */
    static void assign(Owner owner, OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (!optIn(request)) {
            return;
        }
        String lastName = normalizeName(request.getLastName());
        String address = OwnerAddress.normalize(request.getAddress());
        List<Owner> members = membersOf(lastName, address, ownerRepository);
        if (members.isEmpty()) {
            return;
        }
        String householdId = idFor(lastName, address, members);
        owner.setHouseholdId(householdId);
        for (Owner other : members) {
            if (!householdId.equals(other.getHouseholdId())) {
                other.setHouseholdId(householdId);
                ownerRepository.save(other);
            }
        }
    }

    private static boolean optIn(OwnerFieldsDto request) {
        return Boolean.TRUE.equals(request.getSharesHousehold());
    }

    private static List<Owner> membersOf(String lastName, String address,
            OwnerRepository ownerRepository) {
        List<Owner> members = new ArrayList<>();
        for (Owner other : ownerRepository.findAll()) {
            if (lastName.equals(normalizeName(other.getLastName()))
                    && address.equals(OwnerAddress.normalize(other.getAddress()))) {
                members.add(other);
            }
        }
        return members;
    }

    /**
     * The id for a household: an existing member's non-blank id when one exists, otherwise a fresh
     * derived id. {@code null} when there are no members (nothing to join).
     */
    private static String idFor(String lastName, String address, List<Owner> members) {
        if (members.isEmpty()) {
            return null;
        }
        String existingId = null;
        for (Owner other : members) {
            if (other.getHouseholdId() != null && !other.getHouseholdId().isBlank()) {
                existingId = other.getHouseholdId();
            }
        }
        return (existingId != null) ? existingId : deriveId(lastName, address);
    }

    /** Deterministic 16-char upper-hex identifier over the normalized last name and address. */
    private static String deriveId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    /** Lower-case and collapse all runs of whitespace to a single space, trimmed. */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
