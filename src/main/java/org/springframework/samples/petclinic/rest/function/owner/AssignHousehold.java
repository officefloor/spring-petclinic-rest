package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that runs after {@link BuildOwner} has produced the owner. When
 * the request opted in with {@code sharesHousehold: true} and an existing owner already occupies the
 * same household — the same last name and the same address, compared case-insensitively with
 * collapsed whitespace — this owner is joining that household. All members of a household share a
 * single stable {@code householdId}, derived deterministically from the normalized last name and
 * address so every member computes the same value regardless of the order they were created.
 *
 * <p>The shared id is assigned to the new owner and back-filled onto the existing member(s) that do
 * not yet carry it, so both sides of the join report the same {@code householdId}. When the request
 * does not opt in, or there is no existing household to join, no id is assigned (a lone owner has no
 * {@code householdId}). The duplicate-household rejection for non-opted-in requests has already run
 * in {@link RejectDuplicateHousehold}.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                household.add(existing);
            }
        }
        if (household.isEmpty()) {
            // No existing same-household owner: nothing to join, so no shared id.
            return;
        }
        String householdId = householdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner member : household) {
            if (member.getHouseholdId() == null) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }

    /** Lower-case, trim and collapse internal whitespace runs to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Derive the stable household identifier {@code 'H-' + first 12 upper-case hex characters of
     * SHA-256(normalizedLastName + '\n' + normalizedAddress)}. Deterministic, so every owner in the
     * same household resolves to the same value.
     */
    private static String householdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "\n" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return "H-" + sb;
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
