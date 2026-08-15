package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

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
 * {@code householdId}). Duplicate detection has already run in {@link RejectDuplicateIdentity}, which
 * uses the same {@link Households} logic to fold this owner's household id into its identity key.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = Households.normalizeName(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (Households.sameHousehold(lastName, address, existing)) {
                household.add(existing);
            }
        }
        if (household.isEmpty()) {
            // No existing same-household owner: nothing to join, so no shared id.
            return;
        }
        String householdId = Households.householdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner member : household) {
            if (member.getHouseholdId() == null) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }
}
