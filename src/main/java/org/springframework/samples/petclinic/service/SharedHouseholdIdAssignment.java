package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Adopts an existing household's derived {@code householdId} when a new owner knowingly opts into a
 * shared household and another owner with the same normalized (last name, address) already exists.
 * Extracted from {@link OwnerIdentityPolicy} so the identity-key rule stays focused purely on the
 * identity key and its duplicate check.
 */
final class SharedHouseholdIdAssignment {

    private SharedHouseholdIdAssignment() {
    }

    /** Adopt the existing household's id when sharing was opted in and a matching owner already exists. */
    static void assign(ClinicService clinicService, Owner owner, Boolean sharesHousehold) {
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String lastName = OwnerFieldNormalizer.name(owner.getLastName());
        String address = OwnerFieldNormalizer.name(owner.getAddress());
        for (Owner existing : clinicService.findAllOwners()) {
            if (!existing.getId().equals(owner.getId())
                && lastName.equals(OwnerFieldNormalizer.name(existing.getLastName()))
                && address.equals(OwnerFieldNormalizer.name(existing.getAddress()))) {
                owner.setHouseholdId(householdId(lastName, address));
                return;
            }
        }
    }

    /** Derive a stable identifier shared by every owner in the same (last name, address) household. */
    private static String householdId(String lastName, String address) {
        return Sha256Hex.lower(lastName + "|" + address, 8);
    }
}
