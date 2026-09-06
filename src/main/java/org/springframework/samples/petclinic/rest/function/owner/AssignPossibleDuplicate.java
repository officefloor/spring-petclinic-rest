package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft-match duplicate, keyed on the computed {@link Households#id(Owner) householdId}. A
 * new owner that shares an existing owner's household (same last name and postcode) but carries a
 * different telephone is created with {@code possibleDuplicate} set true and {@code possibleDuplicateOf}
 * set to the matching owner's id.
 *
 * <p>A declared household member (request {@code sharesHousehold} true) is NOT a suspected duplicate:
 * it deliberately joins an existing household, so it is created with {@code possibleDuplicate} false.
 * Any other owner reaching this step is not a household duplicate ({@link EnsureUniqueIdentity} has
 * already rejected those), so it too is left unflagged.
 *
 * <p>Runs before {@link SaveOwner} so the new owner has not been persisted yet and is not compared
 * against itself. The earliest matching owner (lowest id) is chosen for stability.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            // Declared household member: a deliberate join, not a suspected duplicate.
            owner.setPossibleDuplicate(Boolean.FALSE);
            return;
        }
        String householdId = owner.getHouseholdId();
        String telephone = owner.getTelephone();
        Owner match = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> householdId.equals(Households.id(existing))
                        && !equalsIgnoreCase(existing.getTelephone(), telephone))
                .min(java.util.Comparator.comparing(Owner::getId))
                .orElse(null);
        if (match != null) {
            owner.setPossibleDuplicate(Boolean.TRUE);
            owner.setPossibleDuplicateOf(match.getId());
        }
        else {
            owner.setPossibleDuplicate(Boolean.FALSE);
        }
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
