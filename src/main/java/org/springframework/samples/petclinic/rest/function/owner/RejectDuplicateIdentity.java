package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The single, consolidated duplicate check. Rejects creating an owner whose whole
 * {@code identityKey} (see {@link OwnerIdentity}) equals an existing owner's; this replaces the
 * former separate telephone, email and household checks. Runs after the telephone, email and
 * household-id have been assigned to the owner (so all three parts of the key are in place) and
 * before {@link SaveOwner}.
 *
 * <p>Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed —
 * only an exact full-key match escalates as a 409 via {@link DuplicateIdentityException}.
 */
public class RejectDuplicateIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.identityKey(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (identityKey.equals(OwnerIdentity.identityKey(existing))) {
                throw new DuplicateIdentityException(
                        "An owner with the same identity (telephone, email and household) already exists");
            }
        }
    }
}
