package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives an owner's {@code membershipLevel} — the numeric rank (1 to 3) returned on the owner DTO
 * and recorded on the create audit line.
 *
 * <p>The level is fixed at creation:
 * <ul>
 *   <li>starts at {@code 1};</li>
 *   <li>adds {@code 1} when an email address is present (non-blank);</li>
 *   <li>adds {@code 1} when the owner is the sole member of their household at creation, i.e. no
 *       earlier owner shares its computed {@code householdId} (see {@link OwnerIdentity#householdIdFor});
 *       an owner with no household — no postcode — is always sole;</li>
 *   <li>capped at {@code 3} — level 4 is reserved for tenure.</li>
 * </ul>
 *
 * <p>Only owners created earlier (lower id) count towards the household size, so the level stays fixed
 * from creation even as the household later grows.
 */
public final class MembershipLevel {

    private static final int MAX_LEVEL = 3;

    private MembershipLevel() {
    }

    /** The owner's membership level, from 1 to 3. */
    public static int of(Owner owner, OwnerRepository ownerRepository) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (isSoleHouseholdMember(owner, ownerRepository)) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
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
