package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's membership number, formatted {@code <customerCode>-M<YY>} where
 * {@code YY} is the last two digits of the registration date's fiscal year (e.g. {@code
 * SMI-0007-M26}). Returns {@code null} when either input is absent, so owners without a
 * customer code or registration date carry no membership number.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    public static String of(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), FiscalYear.of(owner.getRegistrationDate()) % 100);
    }
}
