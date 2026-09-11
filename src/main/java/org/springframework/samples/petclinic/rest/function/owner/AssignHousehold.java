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
 * When a create request opts in via {@code sharesHousehold}, links the new owner into
 * the household of the existing same-lastName owner(s) at the same address by assigning
 * them all a shared {@code householdId}. Runs after {@link BuildOwner} (so the entity
 * exists) and before {@link SaveOwner} (so the new owner is persisted with the id).
 *
 * <p>The household is keyed on the normalized last name and address. If an existing housemate already
 * carries a {@code householdId} it is reused; otherwise a stable id is derived from the
 * household key and back-filled onto the existing housemates, so every member — the new
 * owner and each prior one — shares one identifier. A request that does not opt in is
 * left with a null {@code householdId}.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());

        List<Owner> housemates = new ArrayList<>();
        String existingId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                housemates.add(existing);
                if (existingId == null && existing.getHouseholdId() != null) {
                    existingId = existing.getHouseholdId();
                }
            }
        }

        String householdId = existingId != null ? existingId : deriveHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner mate : housemates) {
            if (!householdId.equals(mate.getHouseholdId())) {
                mate.setHouseholdId(householdId);
                ownerRepository.save(mate);
            }
        }
    }

    /**
     * A stable identifier for a household, derived deterministically from its normalized
     * last name and address so the same household always yields the same id.
     */
    private static String deriveHouseholdId(String lastName, String address) {
        String key = lastName + "|" + address;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("HH-");
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
