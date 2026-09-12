package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's {@code membershipNumber}, formatted {@code '<customerCode>-M<YY>'}
 * where YY is the two-digit {@link FiscalYear} derived from the owner's
 * (business-day-adjusted) registrationDate (e.g. {@code 'NSW-1A2B3C4D-M26'}).
 *
 * <p>This is the one place the membership number is assembled, so the owner response and
 * the creation audit trail read the same value rather than each rebuilding it. Null when
 * the owner has no customerCode or no registrationDate.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    /** The membership number for the given owner, or null when it cannot be derived. */
    public static String of(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M" + FiscalYear.shortYear(owner.getRegistrationDate());
    }
}
