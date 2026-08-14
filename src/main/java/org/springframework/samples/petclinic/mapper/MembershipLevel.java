package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's membership level from its profile completeness and its tenure.
 *
 * <p>The level starts at 1 and gains a point for each maxed pre-tenure factor: a contactable email,
 * a unique name ({@code namesakeCount} of 0) and belonging to a household of three or more members.
 * These factors alone can reach level 3.
 *
 * <p>Level 4 is a loyalty tier: it additionally requires a tenure of more than 365 days since the
 * owner's registration date. Because a newly created owner has zero tenure, a new owner never
 * exceeds level 3 — even one whose pre-tenure factors are all maxed lands at 3, not 4.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for a mapping method and apply it to unrelated fields.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /**
     * The membership level of {@code owner}: 1-3 from its pre-tenure factors, capped at 3 unless the
     * owner has more than 365 days of tenure, in which case level 4 is attainable.
     */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            level++;
        }
        int cap = tenureDays(owner) > 365 ? 4 : 3;
        return Math.min(cap, level);
    }

    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}
