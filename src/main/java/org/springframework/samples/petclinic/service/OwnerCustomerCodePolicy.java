package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: on creation an owner is assigned a {@code customerCode} formatted
 * {@code <CITY3>-<LAST3>-<NNNN>}, where CITY3 is the upper-cased first three letters of the city,
 * LAST3 the upper-cased first three letters of the last name, and NNNN a per-city 4-digit
 * zero-padded sequence equal to one more than the number of owners already in that city
 * (e.g. {@code LON-SMI-0007}). Kept as a small, self-contained unit so the rule can be
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
        String city3 = prefix(owner.getCity());
        String last3 = prefix(owner.getLastName());
        int sequence = ownersInCity(clinicService, owner.getCity()) + 1;
        owner.setCustomerCode(String.format("%s-%s-%04d", city3, last3, sequence));
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }

    private static int ownersInCity(ClinicService clinicService, String city) {
        return (int) clinicService.findAllOwners().stream()
            .filter(other -> city.equalsIgnoreCase(other.getCity()))
            .count();
    }
}
