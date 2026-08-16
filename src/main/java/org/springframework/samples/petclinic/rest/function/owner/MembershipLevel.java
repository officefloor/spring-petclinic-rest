package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives an owner's {@code membershipLevel} — the numeric rank (1 to 4) returned on the owner DTO
 * and recorded on the create audit line.
 *
 * <p>The base level is fixed at creation from these factors:
 * <ul>
 *   <li>starts at {@code 1};</li>
 *   <li>adds {@code 1} when an email address is present (non-blank);</li>
 *   <li>adds {@code 1} when the owner's name was unique at creation, i.e. {@code namesakeCount} is
 *       {@code 0} (see {@link AssignNamesakeCount});</li>
 *   <li>adds {@code 1} when the owner is the sole member of their household at creation, i.e. no
 *       earlier owner shares its computed {@code householdId} (see {@link OwnerIdentity#householdIdFor});
 *       an owner with no household — no postcode — is always sole;</li>
 *   <li>capped at {@code 3} — these pre-tenure factors alone can never reach level 4.</li>
 * </ul>
 *
 * <p>Level {@code 4} is reserved for tenure: it is granted only when the owner's tenure — whole days
 * from {@code registrationDate} to today — exceeds {@code 365} days. A newly created owner has zero
 * tenure, so a new owner never exceeds level 3 (a new owner with an email, a {@code namesakeCount}
 * of 0 and a 3-member household is level 3, not 4).
 *
 * <p>Only owners created earlier (lower id) count towards the household size, so the base level stays
 * fixed from creation even as the household later grows.
 */
public final class MembershipLevel {

    private static final int PRE_TENURE_MAX_LEVEL = 3;

    private static final int MAX_LEVEL = 4;

    private static final long TENURE_DAYS_FOR_LEVEL_4 = 365;

    private MembershipLevel() {
    }

    /** The owner's membership level, from 1 to 4. */
    public static int of(Owner owner, OwnerRepository ownerRepository) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        if (isSoleHouseholdMember(owner, ownerRepository)) {
            level++;
        }
        level = Math.min(level, PRE_TENURE_MAX_LEVEL);
        if (tenureDays(owner) > TENURE_DAYS_FOR_LEVEL_4) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }

    /** Whole days from the owner's {@code registrationDate} to today; {@code 0} when unknown. */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }

    /** Whether no earlier owner shares this owner's household (its computed {@code householdId}). */
    private static boolean isSoleHouseholdMember(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return true;
        }
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
                return false;
            }
        }
        return true;
    }
}
