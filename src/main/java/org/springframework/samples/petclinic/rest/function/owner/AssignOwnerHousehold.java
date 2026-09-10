package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a shared {@code householdId} to a newly built owner that shares a household
 * with one or more existing owners (same last name and address, compared
 * case-insensitively with collapsed whitespace — the same rule as
 * {@link EnsureUniqueOwnerHousehold}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}. Reaching this step with
 * a matching existing owner means the request set {@code sharesHousehold} true (otherwise
 * {@link EnsureUniqueOwnerHousehold} would already have rejected it), so genuine
 * housemates are grouped under one identifier. The identifier is derived deterministically
 * from the normalized last name and address, so every member of a household resolves to
 * the same value regardless of creation order; existing members that predate the household
 * are back-filled with it. An owner with no housemate keeps a {@code null} householdId.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        List<Owner> housemates = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                housemates.add(existing);
            }
        }
        if (housemates.isEmpty()) {
            return; // no shared household
        }
        String householdId = householdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner mate : housemates) {
            if (mate.getHouseholdId() == null || mate.getHouseholdId().isBlank()) {
                mate.setHouseholdId(householdId);
                ownerRepository.save(mate);
            }
        }
    }

    /** Stable 12-char upper-hex identifier derived from the normalized household key. */
    private static String householdId(String lastName, String address) {
        String key = lastName + "\n" + address;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
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

    /** Lower-cases and collapses runs of whitespace to a single space, trimming the ends. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
