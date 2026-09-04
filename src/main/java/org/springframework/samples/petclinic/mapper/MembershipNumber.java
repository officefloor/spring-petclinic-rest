package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

/**
 * Derives the owner's membership number formatted {@code <customerCode>-M<YY>} where
 * YY is the last two digits of the registration date's year (e.g. {@code SMI-0007-M26}).
 * The value is a pure function of the owner's own fields, so it needs no stored state.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    public static String of(String customerCode, LocalDate registrationDate) {
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }
}
