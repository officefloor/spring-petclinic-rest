package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership number '&lt;customerCode&gt;-M&lt;YY&gt;', where YY is the last two
 * digits of the registration date year (e.g. 'SYD-SMI-0007-M26'). The number combines the owner's
 * assigned customer code with the registration year, so it is only available once both have been set.
 */
public final class MembershipNumbers {

    private MembershipNumbers() {
    }

    /**
     * Return the owner's membership number, or null when either the customer code or the
     * registration date is absent.
     */
    public static String of(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }
}
