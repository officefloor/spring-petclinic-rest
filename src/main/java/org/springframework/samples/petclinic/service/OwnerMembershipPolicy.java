package org.springframework.samples.petclinic.service;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner's {@code membershipNumber} is derived as
 * {@code <customerCode>-M<YY>}, where YY is the last two digits of the registration-date
 * year (e.g. {@code SMI-0007-M26}). Kept as a small, self-contained unit so the rule can be
 * applied from the read flow without adding complexity to the mapper, controller, or service.
 */
public final class OwnerMembershipPolicy {

    private OwnerMembershipPolicy() {
    }

    /**
     * Derive the {@code membershipNumber} for the given owner, or {@code null} when the
     * customer code or registration date it is built from is not yet available.
     *
     * @param owner the owner whose membership number to derive
     * @return the membership number, or {@code null} if it cannot be derived
     */
    public static String membershipNumber(Owner owner) {
        String customerCode = owner.getCustomerCode();
        LocalDate registrationDate = owner.getRegistrationDate();
        if (customerCode == null || registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }
}
