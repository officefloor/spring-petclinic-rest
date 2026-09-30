package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * Rejects a create that duplicates an existing owner's identity. The single identity key is the
 * SHA-256 of {@code normalizedTelephone | lowerEmail | soundex(lastName)}
 * (see {@link Owner#getIdentityKey()}); a new owner whose key equals that of any already-persisted,
 * non-deleted owner is rejected 409 via {@link DuplicateOwnerIdentityException}.
 *
 * <p>Soft-deleted owners are ignored, matching the rest of the create endpoint's duplicate and
 * identity checks. The email-domain blocklist runs earlier in the pipeline
 * (see {@link ValidateOwnerEmailDomain}), so a blocklisted email is already rejected 400 before this
 * step is reached. Because the telephone is part of the key, two owners with the same last name and
 * postcode but different telephones are not duplicates here — they fall through to the soft match in
 * {@link DetectOwnerPossibleDuplicate}.
 *
 * <p>Runs after {@link BuildOwner} (so the built owner carries its normalized telephone, lower-cased
 * email and last name) and before {@link SaveOwner}; the new owner has no id yet, so it never matches
 * itself.
 */
public class DetectOwnerDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // an owner never duplicates itself
            }
            if (existing.isDeleted()) {
                continue; // soft-deleted owners are ignored
            }
            if (identityKey.equals(existing.getIdentityKey())) {
                throw new DuplicateOwnerIdentityException(
                        "An owner with the same identity already exists");
            }
        }
    }
}
