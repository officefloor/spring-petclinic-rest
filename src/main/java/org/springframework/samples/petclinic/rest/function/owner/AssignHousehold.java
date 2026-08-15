package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns a shared {@code householdId} when a create-owner request opts into a shared household
 * with {@code sharesHousehold} true and there is already an owner with the same last name and
 * address (the last name compared case-insensitively with collapsed whitespace and the address by
 * its normalized form, see {@link OwnerAddress}). The identifier is stable: derived
 * deterministically from the normalized last name and address, so every owner in the same
 * household receives the same value. It is also written back onto the existing same-household
 * owners so both sides of the join carry the identifier. Runs after {@link BuildOwner} has produced
 * the {@link Owner} and before {@link SaveOwner} persists it, so the new owner is saved with the id.
 *
 * <p>When {@code sharesHousehold} is not set, or there is no existing same-household owner, no
 * identifier is assigned and the owner is saved without one.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = OwnerAddress.normalize(request.getAddress());
        List<Owner> household = new ArrayList<>();
        String existingId = null;
        for (Owner other : ownerRepository.findAll()) {
            if (lastName.equals(normalize(other.getLastName()))
                    && address.equals(OwnerAddress.normalize(other.getAddress()))) {
                household.add(other);
                if (other.getHouseholdId() != null && !other.getHouseholdId().isBlank()) {
                    existingId = other.getHouseholdId();
                }
            }
        }
        if (household.isEmpty()) {
            return;
        }
        String householdId = (existingId != null) ? existingId : deriveHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner other : household) {
            if (!householdId.equals(other.getHouseholdId())) {
                other.setHouseholdId(householdId);
                ownerRepository.save(other);
            }
        }
    }

    /** Deterministic 16-char upper-hex identifier over the normalized last name and address. */
    private static String deriveHouseholdId(String lastName, String address) {
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
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
