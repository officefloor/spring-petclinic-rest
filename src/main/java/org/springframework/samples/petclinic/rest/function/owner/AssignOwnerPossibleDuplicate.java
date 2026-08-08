package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft (non-hard) duplicate. Runs after {@link RejectDuplicateOwnerIdentity}
 * has already rejected exact identity matches (409), so any owner reaching this step is
 * <em>not</em> a hard duplicate. When the new owner nonetheless shares an existing
 * owner's {@code lastName} (compared case-insensitively) and {@code postcode} but with a
 * different (normalized) telephone, the create still proceeds and the new owner is
 * marked as a possible duplicate: its {@code possibleDuplicateOf} is set to the matching
 * owner's id (the earliest such owner when several match). Mutates the {@link Owner} in
 * place so the value is persisted by {@link SaveOwner} and surfaces as
 * {@code possibleDuplicate}/{@code possibleDuplicateOf} on the response.
 *
 * <p>No-ops (leaving {@code possibleDuplicateOf} null, so {@code possibleDuplicate} is
 * false) when the owner has no postcode or no matching existing owner exists.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        if (lastName == null || postcode == null || postcode.isBlank()) {
            return; // nothing to match on
        }
        String telephone = normalizedTelephone(owner);
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never match the owner against itself
            }
            if (!lastName.equalsIgnoreCase(existing.getLastName())) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (telephone.equals(normalizedTelephone(existing))) {
                continue; // same telephone would be a hard, not a possible, duplicate
            }
            if (match == null || existing.getId() < match.getId()) {
                match = existing; // earliest matching owner
            }
        }
        if (match != null) {
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    /** Canonical telephone for comparison: E.164 when parseable, else the raw value. */
    private static String normalizedTelephone(Owner owner) {
        String e164 = E164Telephone.toE164OrNull(owner.getTelephone());
        if (e164 != null) {
            return e164;
        }
        return owner.getTelephone() == null ? "" : owner.getTelephone();
    }
}
