package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code householdMemberCount}: the number of owners in the new owner's
 * household <em>after</em> this create, i.e. the existing members sharing its {@code householdId}
 * plus the new owner itself.
 *
 * <p>Runs after {@link AssignHousehold} (which computes the deterministic {@code householdId} from
 * the owner's lastName and postcode) and before {@link SaveOwner}, so the existing members are
 * already carrying the same id but the new owner is not yet persisted — hence the {@code + 1}. When
 * no existing owner shares the id, the new owner is the sole member and the count is {@code 1}.
 *
 * <p>The count is persisted on the owner as {@code householdMemberCount} for reporting; it no
 * longer influences the membership level (see
 * {@link org.springframework.samples.petclinic.model.MembershipLevel}).
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        int count = 1;
        if (householdId != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    count++;
                }
            }
        }
        owner.setHouseholdMemberCount(count);
    }
}
