package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Create-owner step: the single duplicate check. Rejects the request with a 409 only when
 * the new owner's whole {@link IdentityKey} (normalized telephone, email and household id)
 * equals an existing owner's — a separate telephone, email or household on its own is not a
 * duplicate.
 *
 * <p>Runs after {@link AssignHousehold} (so the new owner already carries its final
 * {@code householdId} and any linked members carry theirs) and before {@link SaveOwner},
 * so both sides of the comparison are in their settled form.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(IdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
