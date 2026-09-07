package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Membership number for pet owners, formatted {@code <customerCode>-M<YY>} where YY is the last two
 * digits of the registration date's year (e.g. {@code NSW-1A2B3C4D-M26}). Derived purely from the
 * owner's own {@code customerCode} and {@code registrationDate}, so it carries no stored state and is
 * seed-independent. Used by the owner mapper to expose {@code membershipNumber} on responses.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    /**
     * The membership number for the given customer code and registration date, or {@code null} when
     * either input is absent (e.g. legacy owners with no customer code or registration date).
     */
    public static String of(String customerCode, LocalDate registrationDate) {
        if (customerCode == null || registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }
}
