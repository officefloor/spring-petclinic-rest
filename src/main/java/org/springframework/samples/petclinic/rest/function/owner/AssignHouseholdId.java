package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link EnsureHouseholdUnique} has allowed the request: when the new owner shares a
 * household with one or more existing owners (same lastName and address, compared the same way as
 * {@link EnsureHouseholdUnique}), it assigns them all the same {@code householdId} — a stable
 * identifier derived deterministically from the normalized lastName and address, so every member of
 * the household resolves to the same value regardless of creation order. Existing members that
 * predate the household are back-filled so both sides carry the shared id. An owner with no household
 * peer keeps a null {@code householdId}. It also records {@code householdSize} — the number of owners
 * that share the household after this create (the existing peers plus this new owner).
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = normalize(owner.getLastName());
        String address = OwnerAddress.normalize(owner.getAddress());
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(OwnerAddress.normalize(existing.getAddress()))) {
                household.add(existing);
            }
        }
        // Record the household size after this create: the existing peers plus this new owner.
        owner.setHouseholdSize(household.size() + 1);
        if (household.isEmpty()) {
            return; // no peer at this lastName + address, so no shared household
        }
        String householdId = OwnerIdentity.deriveHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner existing : household) {
            if (existing.getHouseholdId() == null || existing.getHouseholdId().isBlank()) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** Lower-cased, trimmed, with runs of whitespace collapsed to a single space. */
    private static String normalize(String value) {
        return OwnerIdentity.normalizeName(value);
    }
}
