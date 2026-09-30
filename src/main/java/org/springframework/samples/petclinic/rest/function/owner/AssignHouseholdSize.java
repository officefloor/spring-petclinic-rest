package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Computes the size of the owner's household — this owner plus the existing owners sharing
 * its {@code householdId} (see {@link OwnerHousehold}) — and publishes it for
 * {@link AssignMembership} to read. Runs after the householdId has been assigned and
 * before the owner is saved, so the existing count excludes the owner being created and
 * the {@code + 1} accounts for it.
 *
 * <p>Mirrors {@link AssignNamesakeCount}: a dedicated step scans the repository and stores
 * the derived count, leaving the membership calculation a pure function over already-known
 * values. Published as a variable rather than persisted, as the size is only needed to
 * derive the membership points and is not itself an owner attribute.
 */
public class AssignHouseholdSize {

    public void service(@Val Owner owner, OwnerRepository ownerRepository,
            @Qualified("householdSize") Out<Integer> householdSize) {
        int existingMembers = OwnerHousehold.memberCount(owner.getHouseholdId(), ownerRepository.findAll());
        householdSize.set(existingMembers + 1);
    }
}
