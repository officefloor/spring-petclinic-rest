package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's deterministic {@code householdId} — the first 12 hex characters of SHA-256 over
 * the normalized lastName and postcode (see {@link OwnerIdentity#deriveHouseholdId}). Because the id
 * is a pure function of (lastName, postcode), every owner that shares a lastName and postcode resolves
 * to the same value automatically, with no back-filling and regardless of creation order. It also
 * records {@code householdSize} — the number of owners that share this household after the create
 * (the existing members with the same computed householdId, plus this new owner). Runs before
 * {@link SaveOwner}, so {@link OwnerRepository#findAll()} returns only the owners that predate this
 * create.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = OwnerIdentity.deriveHouseholdId(
                OwnerIdentity.normalizeName(owner.getLastName()), owner.getPostcode());
        owner.setHouseholdId(householdId);

        // Count the existing members of this household (same computed householdId) plus this owner.
        int size = 1;
        for (Owner existing : ownerRepository.findAll()) {
            String existingHousehold = OwnerIdentity.deriveHouseholdId(
                    OwnerIdentity.normalizeName(existing.getLastName()), existing.getPostcode());
            if (householdId.equals(existingHousehold)) {
                size++;
            }
        }
        owner.setHouseholdSize(size);
    }
}
