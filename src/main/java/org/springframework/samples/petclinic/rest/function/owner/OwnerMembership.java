package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single source of truth for an owner's membership scoring: the {@code membershipPoints} an owner
 * has earned and the {@code membershipLevel} those points band into, plus the fiscal-year primitive
 * both the scoring's tenure rule and the owner's fiscal/membership-number formatting share.
 *
 * <p>These are pure functions of an owner's already-stored fields; {@link OwnerMapper} delegates its
 * {@code membershipPoints} and {@code membershipLevel} derivations here, mirroring how it delegates to
 * {@link OwnerIdentity} and {@link OwnerRegion}. The fiscal-year primitive also feeds the FY segment of
 * an owner's {@code memberId} (see {@link MemberId} and {@link AssignMemberId}).
 */
public final class OwnerMembership {

    private OwnerMembership() {
    }

    /**
     * The fiscal year the {@code date} falls in, as an integer. The fiscal year starts on 1 July and is
     * named by the calendar year in which it ends, so 1 July 2026 - 30 June 2027 is fiscal year 2027: a
     * date in July or later belongs to the next calendar year's fiscal year, an earlier date to the
     * current calendar year's.
     */
    public static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The two-digit fiscal-year code for {@code date}: the {@link #fiscalYearOf(LocalDate) fiscal year}
     * it falls in taken mod 100 and zero-padded to two digits (e.g. {@code 27}). This is the FY segment
     * of an owner's {@code memberId} ({@code <REGION>27<HASH8><CHK>}), from which the mapper's
     * {@code fiscalYear} ({@code FY27}) is read back.
     */
    public static String fiscalYearCode(LocalDate date) {
        return String.format("%02d", fiscalYearOf(date) % 100);
    }

    /**
     * The owner's membership points. Starts at 0; add 2 when an email is present; add 1 when the owner's
     * name was unique on creation (namesakeCount is 0); add 2 for a household of 3 or more members; add 3
     * for tenure of one or more elapsed fiscal years (the current fiscal year differs from the fiscal
     * year the registrationDate falls in; fiscal year starting 1 July).
     */
    public static int points(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
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
        if (owner.getRegistrationDate() != null
                && fiscalYearOf(LocalDate.now()) - fiscalYearOf(owner.getRegistrationDate()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, derived by banding {@link #points(Owner)}: level 1 for 0-1 points,
     * level 2 for 2-3, level 3 for 4-5, level 4 for 6 or more.
     *
     * <p>When a {@code membershipLevelCap} is recorded (set at creation from the owner's household — see
     * {@link CapMembershipLevel}), the banded level is capped to it: a new owner's level cannot exceed
     * one above the current maximum level among their household members. A null cap (no existing
     * household member) leaves the banded level unchanged.
     */
    public static int level(Owner owner) {
        int level;
        int points = points(owner);
        if (points >= 6) {
            level = 4;
        }
        else if (points >= 4) {
            level = 3;
        }
        else if (points >= 2) {
            level = 2;
        }
        else {
            level = 1;
        }
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            level = cap;
        }
        return level;
    }
}
