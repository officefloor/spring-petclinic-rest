package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a shared {@code householdId} when a create-owner request opts in with
 * {@code sharesHousehold} and an existing owner shares its household: same last name
 * (compared case-insensitively with runs of whitespace collapsed to a single space) and
 * same address (compared in the normalized form of {@link AddressNormalizer}), the same
 * identity {@link RejectDuplicateHousehold} uses. Both the new owner and
 * every existing household member are assigned the same identifier, derived deterministically
 * from the normalized last name and address, so members always agree on it and it never
 * changes as more join.
 *
 * <p>Runs after {@link BuildOwner} (so the Owner exists) and within the create transaction
 * (so any backfilled members commit with the new owner). When {@code sharesHousehold} is not
 * set, or no existing owner shares the household, no id is assigned.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        String householdId = householdId(lastName, address);
        boolean shared = false;
        for (Owner existing : ownerRepository.findAll()) {
            if (normalize(existing.getLastName()).equals(lastName)
                    && AddressNormalizer.normalize(existing.getAddress()).equals(address)) {
                shared = true;
                if (!householdId.equals(existing.getHouseholdId())) {
                    existing.setHouseholdId(householdId);
                    ownerRepository.save(existing);
                }
            }
        }
        if (shared) {
            owner.setHouseholdId(householdId);
        }
    }

    private static String householdId(String lastName, String address) {
        byte[] key = (lastName + "\n" + address).getBytes(StandardCharsets.UTF_8);
        return UUID.nameUUIDFromBytes(key).toString();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
