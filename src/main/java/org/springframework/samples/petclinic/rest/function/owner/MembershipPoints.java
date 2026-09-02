package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Scores an owner's membership points: starts at 0, adds 2 when an email is present, adds 1 when
 * {@code namesakeCount} is 0, adds 2 for a household of 3 or more members and adds 3 for tenure of
 * more than one elapsed fiscal year.
 */
public final class MembershipPoints {

    private MembershipPoints() {
    }

    public static int of(Owner owner, int namesakeCount, int householdSize) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (namesakeCount == 0) {
            points += 1;
        }
        if (householdSize >= 3) {
            points += 2;
        }
        if (Tenure.fiscalYears(owner) > 1) {
            points += 3;
        }
        return points;
    }

    private static final class Tenure {
        static int fiscalYears(Owner owner) {
            LocalDate registration = owner.getRegistrationDate();
            return registration == null ? 0 : FiscalYear.of(LocalDate.now()) - FiscalYear.of(registration);
        }
    }
}
