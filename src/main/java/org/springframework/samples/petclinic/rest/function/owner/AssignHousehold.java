package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When a create request sets {@code sharesHousehold} true and an existing owner already shares this
 * owner's last name and address, both are given the same {@code householdId} so the shared household
 * is discoverable. The id is a stable identifier derived from the normalized last name and address
 * (upper-case hex prefix of their SHA-256), so every member of a household computes the same value.
 *
 * <p>Last name is compared case-insensitively with collapsed whitespace and address in its normalized
 * form (see {@link AddressNormalizer}), matching {@link EnsureUniqueIdentity}, and the id is derived
 * from those same normalized values (via {@link OwnerIdentityKey}). Runs after {@link BuildOwner} within the create transaction: it sets
 * the id on the new owner (persisted by {@link SaveOwner}) and saves any existing members that did not
 * yet carry it. A request without {@code sharesHousehold}, or one with no matching owner, is left
 * untouched.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = OwnerIdentityKey.normalizeName(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        java.util.List<Owner> household = new java.util.ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerIdentityKey.normalizeName(existing.getLastName()).equals(lastName)
                    && AddressNormalizer.normalize(existing.getAddress()).equals(address)) {
                household.add(existing);
            }
        }
        if (household.isEmpty()) {
            return;
        }
        String householdId = OwnerIdentityKey.householdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner member : household) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }
}
