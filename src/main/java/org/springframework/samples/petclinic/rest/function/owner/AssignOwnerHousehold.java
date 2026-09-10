package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a shared {@code householdId} to a newly built owner that shares a household
 * with one or more existing owners (same last name and address, compared
 * case-insensitively with collapsed whitespace — see {@link OwnerHousehold}).
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}, so genuine housemates are
 * grouped under one identifier. The identifier is derived deterministically from the
 * normalized last name and address, so every member of a household resolves to the same
 * value regardless of creation order; existing members that predate the household are
 * back-filled with it. An owner with no housemate keeps a {@code null} householdId. The
 * same derivation feeds the duplicate key in {@link EnsureUniqueOwnerIdentity}.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = OwnerHousehold.normalizeName(owner.getLastName());
        String address = OwnerHousehold.normalizeAddress(owner.getAddress());
        List<Owner> housemates = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(OwnerHousehold.normalizeName(existing.getLastName()))
                    && address.equals(OwnerHousehold.normalizeAddress(existing.getAddress()))) {
                housemates.add(existing);
            }
        }
        if (housemates.isEmpty()) {
            return; // no shared household
        }
        String householdId = OwnerHousehold.id(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner mate : housemates) {
            if (mate.getHouseholdId() == null || mate.getHouseholdId().isBlank()) {
                mate.setHouseholdId(householdId);
                ownerRepository.save(mate);
            }
        }
    }
}
