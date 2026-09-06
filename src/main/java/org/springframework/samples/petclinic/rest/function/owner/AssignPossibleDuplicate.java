package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft-match duplicate. A new owner is a suspected duplicate of an existing owner when their
 * {@link OwnerIdentity#key(Owner) identityKey}s DIFFER (so it was not rejected as a hard duplicate)
 * yet they share the same {@link Soundex} of the last name AND the same postcode. It is created with
 * {@code possibleDuplicate} set true and {@code possibleDuplicateOf} set to the matching owner's id.
 *
 * <p>The soft match is intentionally broader than the identity key: two owners at the same last name
 * (phonetically) and postcode but with different telephones are no longer a hard household duplicate,
 * so this step surfaces them for review instead of rejecting them.
 *
 * <p>A declared household member (request {@code sharesHousehold} true) is NOT a suspected duplicate:
 * it deliberately joins an existing household, so it is created with {@code possibleDuplicate} false.
 *
 * <p>Runs before {@link SaveOwner} so the new owner has not been persisted yet and is not compared
 * against itself. Soft-deleted owners are ignored. The earliest matching owner (lowest id) is chosen
 * for stability.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            // Declared household member: a deliberate join, not a suspected duplicate.
            owner.setPossibleDuplicate(Boolean.FALSE);
            return;
        }
        String identityKey = OwnerIdentity.key(owner);
        String soundex = Soundex.of(owner.getLastName());
        String postcode = postcode(owner);
        Owner match = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> !Boolean.TRUE.equals(existing.getDeleted())) // ignore soft-deleted owners
                .filter(existing -> !identityKey.equals(OwnerIdentity.key(existing)) // not a hard duplicate
                        && soundex.equals(Soundex.of(existing.getLastName()))
                        && postcode.equals(postcode(existing)))
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

    private static String postcode(Owner owner) {
        String postcode = owner.getPostcode();
        return postcode == null ? "" : postcode.trim();
    }
}
