package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Household assignment for {@code POST /owners}. When the request opts in via
 * {@code sharesHousehold} true, the new owner joins the household of any existing owner it
 * {@link OwnerHousehold#matches matches}. Both the joining owner and the matched existing
 * owners are assigned the same {@code householdId} — a stable shared identifier (see
 * {@link OwnerHousehold#deriveId}) — so the household is discoverable from any of its members.
 * If a matched owner already carries a {@code householdId}, that value is reused so the
 * identifier stays stable as further members join.
 *
 * <p>Runs within the create transaction (after {@link BuildOwner}/{@link AssignCustomerCode},
 * before {@link SaveOwner}) so the back-filled identifiers on existing owners commit with the
 * new owner. Requests without {@code sharesHousehold} leave {@code householdId} null.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        List<Owner> matches = new ArrayList<>();
        String householdId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (OwnerHousehold.matches(request, existing)) {
                matches.add(existing);
                if (householdId == null && existing.getHouseholdId() != null) {
                    householdId = existing.getHouseholdId();
                }
            }
        }
        if (householdId == null) {
            householdId = OwnerHousehold.deriveId(request);
        }
        owner.setHouseholdId(householdId);
        for (Owner existing : matches) {
            if (!householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }
}
