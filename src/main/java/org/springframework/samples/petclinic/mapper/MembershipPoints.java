package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;

/**
 * Scores an owner's membership points: 2 when an email is on file, 1 when the owner had
 * no namesakes at creation (namesakeCount is 0), 2 for a household of three or more, and
 * 3 when the owner's tenure exceeds one elapsed fiscal year. {@link MembershipLevel} maps
 * the total to a level.
 */
public final class MembershipPoints {

    private static final int TENURE_FISCAL_YEARS = 1;

    private MembershipPoints() {
    }

    public static int of(Integer namesakeCount, String email, LocalDate registrationDate,
            boolean goldHousehold) {
        int points = 0;
        if (email != null && !email.isBlank()) {
            points += 2;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        if (goldHousehold) {
            points += 2;
        }
        if (registrationDate != null
                && FiscalYear.of(LocalDate.now()) - FiscalYear.of(registrationDate) > TENURE_FISCAL_YEARS) {
            points += 3;
        }
        return points;
    }
}
