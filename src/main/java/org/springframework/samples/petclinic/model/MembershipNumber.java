package org.springframework.samples.petclinic.model;

/**
 * The owner's membership number, formatted as {@code <customerCode>-M<YY>} where {@code YY}
 * is the last two digits of the {@link FiscalYear fiscal year} of the (business-day-adjusted)
 * registration date, e.g. {@code SMI-0007-M26}.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    /** The membership number for the given owner (see {@link MembershipNumber}). */
    public static String of(Owner owner) {
        String yy = String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100);
        return owner.getCustomerCode() + "-M" + yy;
    }
}
