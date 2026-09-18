package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the newly built owner as a possible (soft) duplicate of an existing one. A soft match is an
 * existing owner in the same household — same deterministic {@link Owner#getHouseholdId() householdId}
 * (last name and postcode) — but reachable on a different telephone. When one is found the earliest
 * such owner's id is recorded via {@link Owner#setPossibleDuplicateOf(Integer)}, which drives the
 * {@code possibleDuplicate} / {@code possibleDuplicateOf} response fields.
 *
 * <p>A {@link DeclaredHouseholdMember declared household member} — one admitted into an occupied
 * household via {@code sharesHousehold} — is deliberate, not suspected, so it is never flagged. Every
 * other same-household owner has already been rejected as a household duplicate by
 * {@link EnsureUniqueHousehold}. Owners without a postcode belong to no household and never soft-match.
 *
 * <p>Runs after {@link EnsureUniqueIdentity} and before {@link SaveOwner}, so the flag is persisted
 * with the owner.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, @Val DeclaredHouseholdMember declaredMember,
            OwnerRepository ownerRepository) {
        if (declaredMember != null) {
            return;
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return;
        }
        ownerRepository.findByLastName(owner.getLastName()).stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId())
                && existing.getTelephone() != null
                && !existing.getTelephone().equals(owner.getTelephone()))
            .map(Owner::getId)
            .filter(id -> id != null)
            .min(Integer::compareTo)
            .ifPresent(owner::setPossibleDuplicateOf);
    }
}
