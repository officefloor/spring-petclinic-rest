package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} (so the request telephone
 * is E.164 and the email lower-cased) and before {@link BuildOwner}. Rejects the request with
 * {@link DuplicateIdentityException} (handled as 409) when it collides with an existing owner.
 *
 * <p>The household is keyed on (lastName, postcode): the new owner's deterministic
 * {@link OwnerIdentity#householdId householdId} is computed from those fields, and an existing owner
 * with the same household id is a <em>household duplicate</em>. A second owner in an existing household
 * is therefore rejected — <em>unless</em> the request opts in with {@code sharesHousehold: true}, which
 * bypasses this whole duplicate block so the owner is created as a declared household member.
 *
 * <p>Independently of the household, an existing owner with the same normalized telephone and email is a
 * <em>contact duplicate</em> and is also rejected (unless the request opted in above).
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        // A declared household member opts in with sharesHousehold=true and bypasses the whole
        // duplicate block, so it is created even though it shares an existing owner's household.
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = OwnerIdentity.householdId(request.getLastName(), request.getPostcode());
        String contactKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), null);
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            // A soft-deleted owner is no longer a live identity: skip it, so a duplicate that would
            // otherwise block is allowed when its only match has been deleted.
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            // Household duplicate: same deterministic householdId (same lastName + postcode).
            String existingHousehold =
                    OwnerIdentity.householdId(existing.getLastName(), existing.getPostcode());
            if (householdId.equals(existingHousehold)) {
                throw new DuplicateIdentityException(identityKey);
            }
            // Contact duplicate: same normalized telephone and email, regardless of household.
            String existingContact = OwnerIdentity.key(OwnerIdentity.toE164(existing.getTelephone()),
                    OwnerIdentity.normalizeEmail(existing.getEmail()), null);
            if (contactKey.equals(existingContact)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
