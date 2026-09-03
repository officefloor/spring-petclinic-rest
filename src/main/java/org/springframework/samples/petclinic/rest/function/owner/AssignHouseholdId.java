package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link EnsureHouseholdUnique} has allowed the request: when the new owner shares a
 * household with one or more existing owners (same lastName and address, compared the same way as
 * {@link EnsureHouseholdUnique}), it assigns them all the same {@code householdId} — a stable
 * identifier derived deterministically from the normalized lastName and address, so every member of
 * the household resolves to the same value regardless of creation order. Existing members that
 * predate the household are back-filled so both sides carry the shared id. An owner with no household
 * peer keeps a null {@code householdId}.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = normalize(owner.getLastName());
        String address = OwnerAddress.normalize(owner.getAddress());
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(OwnerAddress.normalize(existing.getAddress()))) {
                household.add(existing);
            }
        }
        if (household.isEmpty()) {
            return; // no peer at this lastName + address, so no shared household
        }
        String householdId = deriveHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner existing : household) {
            if (existing.getHouseholdId() == null || existing.getHouseholdId().isBlank()) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** {@code H-} plus the first 12 upper-case hex chars of SHA-256(lastName + '|' + address). */
    private static String deriveHouseholdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + '|' + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("H-");
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Lower-cased, trimmed, with runs of whitespace collapsed to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
