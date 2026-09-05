package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Derives the {@code membershipNumber} formatted {@code <customerCode>-M<YY>}, where YY is
 * the last two digits of the registrationDate year (e.g. {@code SMI-0007-M26}).
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    public static String of(String customerCode, LocalDate registrationDate) {
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }
}
