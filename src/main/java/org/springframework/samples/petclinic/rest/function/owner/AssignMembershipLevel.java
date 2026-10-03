package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipPoints} and derived numeric
 * {@code membershipLevel} on create. Points start at 0 and accumulate: add 2 when an
 * email address is present, add 1 when {@code namesakeCount} is 0, add 2 when the owner
 * belongs to a household of three or more members, and add 3 for tenure of more than
 * one elapsed fiscal year (the fiscal year starts on 1 July). The points then map to a
 * level: 1 for 0-1 points, 2 for 2-3 points, 3 for 4-5 points and 4 for 6 or more
 * points. A newly created owner has zero tenure (its registration date is the server
 * date), so a new owner never earns the tenure points.
 * Runs after {@code namesakeCount} and {@code householdSize} have been counted and
 * before the owner is saved, so both values are fixed at creation time.
 */
public class AssignMembershipLevel {

    private static final int TENURE_FISCAL_YEARS_FOR_POINTS = 1;

    public void service(@Val Owner owner) {
        int points = 0;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            points += 2;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        Integer householdSize = owner.getHouseholdSize();
        if (householdSize != null && householdSize >= 3) {
            points += 2;
        }
        if (tenureFiscalYears(owner) > TENURE_FISCAL_YEARS_FOR_POINTS) {
            points += 3;
        }
        owner.setMembershipPoints(points);
        owner.setMembershipLevel(levelForPoints(points));
    }

    private static int levelForPoints(int points) {
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }

    private static int tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return OwnerFiscalYear.elapsedFiscalYears(registrationDate, LocalDate.now());
    }
}
