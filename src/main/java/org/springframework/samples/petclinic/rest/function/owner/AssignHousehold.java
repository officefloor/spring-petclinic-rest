package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When the create request sets {@code sharesHousehold} true and the new owner joins a household
 * already occupied by an existing owner (same last name and address, see {@link HouseholdNormalizer}),
 * stamps the new owner and every existing member of that household with the same stable
 * {@link HouseholdNormalizer#id(String, String) household id}. Without {@code sharesHousehold} the
 * request never reaches here as a joiner — {@link EnsureUniqueHousehold} rejects the collision first —
 * so a lone owner keeps a null household id. Runs after {@link BuildOwner} and mutates the built
 * {@link Owner} in place; existing members are re-saved so a later read returns the shared id too.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String key = HouseholdNormalizer.key(owner.getLastName(), owner.getAddress());
        boolean joinsHousehold = false;
        String householdId = HouseholdNormalizer.id(owner.getLastName(), owner.getAddress());
        for (Owner member : ownerRepository.findAll()) {
            if (!key.equals(HouseholdNormalizer.key(member.getLastName(), member.getAddress()))) {
                continue;
            }
            joinsHousehold = true;
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
        if (joinsHousehold) {
            owner.setHouseholdId(householdId);
        }
    }
}
