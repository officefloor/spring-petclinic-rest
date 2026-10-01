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
 * donates a shared {@code householdId} so both owners carry one identifier (a fresh one is minted and
 * stamped onto that donor when it has none yet). Otherwise (not sharing, or sharing with no existing
 * household to join) the owner starts its own household and is left with no shared identifier.
 *
 * <p>The {@code householdId} is a component of the owner's
 * {@link org.springframework.samples.petclinic.model.OwnerIdentity} key, so leaving solo
 * owners without one is deliberate: duplicate detection then turns on the normalized telephone and
 * email, and two owners who merely share a household but differ in telephone remain distinct.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            OwnerHousehold household = OwnerHousehold.of(owner);
            for (Owner existing : ownerRepository.findAll()) {
                if (household.equals(OwnerHousehold.of(existing))) {
                    String householdId = existing.getHouseholdId();
                    if (householdId == null) {
                        // First time this household is shared: mint one id for both members.
                        householdId = UUID.randomUUID().toString();
                        existing.setHouseholdId(householdId);
                        ownerRepository.save(existing);
                    }
                    owner.setHouseholdId(householdId);
                    return;
                }
            }
        }
        // Solo owner (or no household to join): no shared identifier.
        owner.setHouseholdId(null);
    }
}
