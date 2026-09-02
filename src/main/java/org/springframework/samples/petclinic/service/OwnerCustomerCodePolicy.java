package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: on creation an owner is assigned a {@code customerCode} formatted
 * {@code <LAST3>-<NNNN>}, where LAST3 is the upper-cased first three letters of the last name
 * and NNNN is a global 4-digit zero-padded sequence equal to one more than the current number
 * of owners (e.g. {@code SMI-0007}). Kept as a small, self-contained unit so the rule can be
 * applied from the create flow without adding complexity to the controller or service.
 */
public final class OwnerCustomerCodePolicy {

    private OwnerCustomerCodePolicy() {
    }

    /**
     * Assign the {@code customerCode} for a newly created owner.
     *
     * @param clinicService source of the current owner count
     * @param owner         the owner being created
     */
    public static void assignCustomerCode(ClinicService clinicService, Owner owner) {
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = clinicService.findAllOwners().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", last3, sequence));
    }
}
