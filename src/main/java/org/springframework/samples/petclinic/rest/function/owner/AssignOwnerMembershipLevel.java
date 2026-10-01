package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipPoints} and derived numeric {@code membershipLevel} at
 * creation time.
 *
 * <p>Points start at 0 and gain for each qualifying factor: +2 for a non-blank email address, +1
 * for a {@code namesakeCount} of 0, +2 for a household of {@value #HOUSEHOLD_THRESHOLD} or more
 * members, and +3 for tenure of more than {@value #TENURE_THRESHOLD_FISCAL_YEARS} elapsed fiscal
 * year(s) (counted from the owner's {@code registrationDate}, the financial year starting 1 July).
 *
 * <p>Points map to a level: 0-1 points -> level 1, 2-3 -> level 2, 4-5 -> level 3, 6 or more ->
 * level 4. A newly created owner registers as of the current date and so has zero tenure, which
 * means the tenure factor never applies at creation.
 *
 * <p>Runs in the {@code POST /api/owners} pipeline after {@link CountOwnerNamesakes} (so
 * {@code namesakeCount} is already frozen) and {@link CountOwnerHousehold} (so {@code householdSize}
 * is frozen), and before {@link SaveOwner} (so the values are persisted) and
 * {@link AuditOwnerCreated} (so the audit line can record the level). Frozen at creation and
 * returned unchanged on later reads.
 */
public class AssignOwnerMembershipLevel {

    /** Household size at or above which the household factor applies. */
    static final int HOUSEHOLD_THRESHOLD = 3;

    /** Elapsed fiscal years an owner's tenure must exceed to earn the tenure factor. */
    static final long TENURE_THRESHOLD_FISCAL_YEARS = 1;

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
        if (householdSize != null && householdSize >= HOUSEHOLD_THRESHOLD) {
            points += 2;
        }
        if (tenureFiscalYears(owner) > TENURE_THRESHOLD_FISCAL_YEARS) {
            points += 3;
        }
        owner.setMembershipPoints(points);
        owner.setMembershipLevel(levelForPoints(points));
    }

    /** Maps points to a level: 0-1 -> 1, 2-3 -> 2, 4-5 -> 3, 6 or more -> 4. */
    private static int levelForPoints(int points) {
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    /**
     * The owner's tenure in whole elapsed fiscal years: the number of 1 July boundaries crossed
     * between their {@code registrationDate} and the current date, never negative, and 0 when no
     * registration date is recorded. A just-created owner registers in the current fiscal year, so
     * its tenure is 0.
     */
    private static long tenureFiscalYears(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return OwnerFiscalYear.elapsedFiscalYears(registrationDate, LocalDate.now());
    }
}
