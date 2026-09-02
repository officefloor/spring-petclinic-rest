package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: on creation an owner records its {@code namesakeCount}, the number of existing
 * owners that already share the same first and last name (compared case-insensitively) at the
 * moment before this owner is created. Kept as a small, self-contained unit so the rule can be
 * applied from the create flow without adding complexity to the controller or service.
 */
public final class OwnerNamesakePolicy {

    private OwnerNamesakePolicy() {
    }

    /**
     * Record the {@code namesakeCount} for a newly created owner.
     *
     * @param clinicService source of the existing owners
     * @param owner         the owner being created
     */
    public static void assignNamesakeCount(ClinicService clinicService, Owner owner) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner existing : clinicService.findAllOwners()) {
            if (firstName.equalsIgnoreCase(existing.getFirstName())
                && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }
}
