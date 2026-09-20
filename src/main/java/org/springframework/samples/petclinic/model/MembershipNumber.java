package org.springframework.samples.petclinic.model;

/**
 * The owner's membership number, formatted as {@code <customerCode>-M<YY>} where {@code YY}
 * is the last two digits of the registration date year, e.g. {@code SMI-0007-M26}.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    /** The membership number for the given owner (see {@link MembershipNumber}). */
    public static String of(Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        return owner.getCustomerCode() + "-M" + yy;
    }
}
