package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns a shared {@code householdId} when a create request opts in with
 * {@code sharesHousehold: true} and an existing owner already shares the household — the same
 * lastName and address, compared case-insensitively with collapsed whitespace (matching
 * {@link CheckOwnerHouseholdUnique}, which allows the duplicate through in that case).
 *
 * <p>The identifier is a <em>stable</em> value derived deterministically from the normalized
 * lastName and address, so every owner in the same household receives the same id regardless of
 * creation order. Both the joining owner and every existing household member are updated to carry
 * it, so the household is linked from either side.
 *
 * <p>Runs after {@link BuildOwner} (the entity exists) and before {@link SaveOwner}, under the
 * request transaction so the updates to existing owners commit together with the new owner. When
 * the request does not opt in, or no existing household member is found, no id is assigned.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
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
            return;
        }
        String householdId = deriveHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner existing : household) {
            if (!householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** {@code HH-} followed by the first 12 upper-case hex characters of SHA-256(lastName|address),
     *  computed over the already-normalized values so it is identical for every household member. */
    private static String deriveHouseholdId(String normalizedLastName, String normalizedAddress) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((normalizedLastName + "|" + normalizedAddress).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return "HH-" + sb;
        }
        catch (Exception ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
