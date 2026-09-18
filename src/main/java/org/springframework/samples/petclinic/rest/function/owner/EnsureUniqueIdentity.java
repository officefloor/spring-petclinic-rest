package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Create-owner step: the duplicate block. Two owners are the same person when they share the
 * same {@link IdentityKey} (normalized telephone, email and the {@link Soundex} of the last
 * name), so a request whose key already belongs to an existing owner is rejected with a 409.
 * Duplicate detection is the single identity key: an owner sharing another's last name and
 * postcode but with a different telephone has a different key and is admitted here (later
 * recorded as a {@link FlagPossibleDuplicate possible duplicate}).
 *
 * <p>Runs after {@link EnsureEmailNotDisposable} (so the email-domain blocklist is applied
 * first) and before {@link FlagPossibleDuplicate} and {@link SaveOwner}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer occupies its identity
            }
            if (identityKey.equals(IdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
