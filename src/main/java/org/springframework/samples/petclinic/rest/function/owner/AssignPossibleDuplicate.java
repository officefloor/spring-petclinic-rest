package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-duplicate detection for {@code POST /owners}. A request that cleared the hard-duplicate
 * guard ({@link CheckOwnerIdentityUnique}) is still created, but is flagged as a <em>possible</em>
 * duplicate when it shares an existing owner's {@code lastName} and {@code postcode} while carrying
 * a <em>different</em> telephone. The matching owner's id is recorded in {@code possibleDuplicateOf};
 * when there is no such match both {@code possibleDuplicate} is {@code false} and
 * {@code possibleDuplicateOf} is {@code null}.
 *
 * <p>Runs within the create transaction after {@link BuildOwner} (so the built owner's telephone is
 * already in its stored E.164 form) and before {@link SaveOwner} (so {@code findAll} excludes the
 * owner being created). When several existing owners match, the one with the lowest id is recorded
 * so the result is deterministic. A request without a postcode cannot share one, so it is never a
 * possible duplicate.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String lastName = owner.getLastName();
        String telephone = owner.getTelephone();

        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            boolean sameLastName = lastName != null && lastName.equalsIgnoreCase(existing.getLastName());
            boolean samePostcode = postcode.equals(existing.getPostcode());
            boolean differentTelephone = telephone == null || !telephone.equals(existing.getTelephone());
            if (sameLastName && samePostcode && differentTelephone) {
                if (match == null || existing.getId() < match.getId()) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }
}
