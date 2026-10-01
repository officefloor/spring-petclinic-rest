package org.springframework.samples.petclinic.rest.function.owner;

import java.util.UUID;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs after {@link BuildOwner} on {@code POST /api/owners} and assigns the new owner's stable
 * {@code householdId}.
 *
 * <p>When the request opted in via {@code sharesHousehold} true, the new owner joins an existing
 * household: the first already-persisted owner in the same household (see {@link OwnerHousehold})
 * donates its {@code householdId} so both owners share one identifier. Otherwise (not sharing, or
 * sharing with no existing household to join) a fresh random identifier is minted so every owner
 * always has a non-blank {@code householdId}.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            OwnerHousehold household = OwnerHousehold.of(owner);
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getHouseholdId() != null && household.equals(OwnerHousehold.of(existing))) {
                    owner.setHouseholdId(existing.getHouseholdId());
                    return;
                }
            }
        }
        owner.setHouseholdId(UUID.randomUUID().toString());
    }
}
