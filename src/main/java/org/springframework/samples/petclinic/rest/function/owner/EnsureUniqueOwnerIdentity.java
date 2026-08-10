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
 * <p>Duplicate detection is expressed through the single {@code identityKey} = normalized telephone,
 * email and {@link OwnerIdentity#householdId householdId} (derived from lastName + postcode). The
 * request collides only when its <em>whole</em> identityKey equals an existing owner's: same telephone,
 * same email <em>and</em> same household. A differing component — a distinct telephone, email, or
 * household — is a separate identity that is allowed, so several owners can share a household as long as
 * their contact details differ (each is then a household member whose membership level is capped by
 * {@link CapMembershipLevel}). {@code sharesHousehold: true} still bypasses the check entirely.
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
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            // A soft-deleted owner is no longer a live identity: skip it, so a duplicate that would
            // otherwise block is allowed when its only match has been deleted.
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            // Duplicate only when the whole identityKey matches: same telephone, email AND household.
            String existingHousehold =
                    OwnerIdentity.householdId(existing.getLastName(), existing.getPostcode());
            String existingKey = OwnerIdentity.key(OwnerIdentity.toE164(existing.getTelephone()),
                    OwnerIdentity.normalizeEmail(existing.getEmail()), existingHousehold);
            if (identityKey.equals(existingKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
