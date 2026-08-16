package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives an owner's {@code membershipTier} — the display-only rank returned on the owner DTO.
 *
 * <p>Precedence, highest first:
 * <ul>
 *   <li>{@code GOLD} — the owner belongs to a household (a non-null {@code householdId}) whose
 *       membership is 3 or more, counting every owner sharing that identifier. Households are stamped
 *       by {@link AssignHousehold} at create time, so once a household reaches three members every one
 *       of them reports GOLD.</li>
 *   <li>{@code SILVER} — the owner's name is unique ({@code namesakeCount == 0}) and an email is on
 *       file.</li>
 *   <li>{@code BRONZE} — otherwise.</li>
 * </ul>
 *
 * <p>The tier is computed at read time (not persisted) so a household crossing the GOLD threshold is
 * reflected immediately for members created before the threshold was reached.
 */
public final class MembershipTier {

    private static final int GOLD_HOUSEHOLD_SIZE = 3;

    private MembershipTier() {
    }

    /** The full tier, applying the GOLD household rule over the base BRONZE/SILVER rules. */
    public static String of(Owner owner, OwnerRepository ownerRepository) {
        if (owner.getHouseholdId() != null && householdSize(owner, ownerRepository) >= GOLD_HOUSEHOLD_SIZE) {
            return "GOLD";
        }
        return baseTier(owner);
    }

    /** The pre-GOLD ranking from the owner's own fields alone. */
    private static String baseTier(Owner owner) {
        boolean silver = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0
                && owner.getEmail() != null && !owner.getEmail().isBlank();
        return silver ? "SILVER" : "BRONZE";
    }

    /** The number of owners sharing this owner's household identifier. */
    private static long householdSize(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }
}
