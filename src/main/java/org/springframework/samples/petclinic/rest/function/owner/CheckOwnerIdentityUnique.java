package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The duplicate check, rejecting a create-owner request with a {@link DuplicateOwnerException} (409)
 * on a single ground: the whole derived {@link OwnerIdentityKey} — the SHA-256 hex over
 * {@code telephone|lowerEmail|soundex(lastName)} — equals an existing owner's. Duplicate detection is
 * now this one identity key; the earlier separate household-duplicate ground (a shared computed
 * {@code householdId}) no longer applies, so two owners with the same last name and postcode but
 * different telephones have different keys and are both accepted (the second is flagged a soft match
 * by {@link AssignPossibleDuplicate}).
 *
 * <p>The email-domain blocklist is applied first, upstream, by {@link RejectDisposableEmailDomain}.
 * A soft-deleted owner is ignored. Runs after {@link BuildOwner} so the new owner's telephone, email
 * and last name are in their stored, normalized form, and before {@link SaveOwner} so the
 * not-yet-persisted new owner is compared only against existing owners.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        String identityKey = OwnerIdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never a duplicate of itself
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is ignored by the duplicate check
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateOwnerException(identityKey);
            }
        }
    }
}
