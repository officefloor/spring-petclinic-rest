package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives an owner's {@code membershipPoints} and the {@code membershipLevel} banded from them — both
 * returned on the owner DTO; the level is also recorded on the create audit line.
 *
 * <p>Points are scored at creation, starting at {@code 0}:
 * <ul>
 *   <li>adds {@code 2} when an email address is present (non-blank);</li>
 *   <li>adds {@code 1} when the owner's name was unique at creation, i.e. {@code namesakeCount} is
 *       {@code 0} (see {@link AssignNamesakeCount});</li>
 *   <li>adds {@code 2} when the owner belongs to a household of 3 or more — its computed
 *       {@code householdId} is shared with at least two earlier owners (see
 *       {@link OwnerIdentity#householdIdFor}); an owner with no household — no postcode — is a
 *       household of one;</li>
 *   <li>adds {@code 3} when the owner's tenure — whole days from {@code registrationDate} to today —
 *       exceeds {@code 365} days.</li>
 * </ul>
 *
 * <p>The points map to a level of 1 to 4: {@code 0-1} points is level {@code 1}, {@code 2-3} is
 * level {@code 2}, {@code 4-5} is level {@code 3}, and {@code 6} or more is level {@code 4}. A newly
 * created owner has zero tenure, so a new owner scores at most {@code 5} points (2 + 1 + 2) and never
 * reaches level 4.
 *
 * <p>Only owners created earlier (lower id) count towards the household size, so the base points stay
 * fixed from creation even as the household later grows.
 */
public final class MembershipLevel {

    private static final int EMAIL_POINTS = 2;

    private static final int UNIQUE_NAME_POINTS = 1;

    private static final int LARGE_HOUSEHOLD_POINTS = 2;

    private static final int TENURE_POINTS = 3;

    private static final int LARGE_HOUSEHOLD_SIZE = 3;

    private static final long TENURE_DAYS_FOR_POINTS = 365;

    private MembershipLevel() {
    }

    /** The owner's membership level, from 1 to 4, banded from {@link #points}. */
    public static int of(Owner owner, OwnerRepository ownerRepository) {
        return level(points(owner, ownerRepository));
    }

    /** The owner's membership points (0 or more). */
    public static int points(Owner owner, OwnerRepository ownerRepository) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += EMAIL_POINTS;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += UNIQUE_NAME_POINTS;
        }
        if (householdSize(owner, ownerRepository) >= LARGE_HOUSEHOLD_SIZE) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (tenureDays(owner) > TENURE_DAYS_FOR_POINTS) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /** The membership level (1 to 4) for a points total: 0-1 → 1, 2-3 → 2, 4-5 → 3, 6+ → 4. */
    public static int level(int points) {
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

    /** Whole days from the owner's {@code registrationDate} to today; {@code 0} when unknown. */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }

    /** The owner's household size: this owner plus every earlier owner sharing its {@code householdId}. */
    private static int householdSize(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return 1;
        }
        int size = 1;
        Integer ownerId = owner.getId();
        for (Owner other : ownerRepository.findAll()) {
            Integer otherId = other.getId();
            if (otherId == null) {
                continue;
            }
            if (ownerId != null && (otherId.equals(ownerId) || otherId >= ownerId)) {
                continue; // only owners that already existed at creation count
            }
            if (householdId.equals(other.getHouseholdId())) {
                size++;
            }
        }
        return size;
    }
}
