package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Formats an owner's membership number as '&lt;customerCode&gt;-M&lt;YY&gt;' where YY is the last two
 * digits of the {@link FiscalYear fiscal year} of the registration date, e.g. 'NSW-1A2B3C4D-M26'.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    /**
     * The membership number for {@code owner}, or {@code null} when the owner or the customer code or
     * registration date it is derived from is {@code null}.
     */
    public static String of(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
                FiscalYear.of(owner.getRegistrationDate()) % 100);
    }
}
