package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Duplicate-detection step of {@code POST /api/owners}: rejects the request 409 when the new owner
 * duplicates an existing one. Because the {@code householdId} is now deterministic &mdash; derived
 * from the last name and postcode (see {@link OwnerIdentity#householdId(String, String)}) &mdash;
 * two owners sharing a last name and postcode belong to the <em>same</em> household, and a second
 * such owner is a household duplicate.
 *
 * <p>Only an <b>exact identity</b> conflict is rejected: the whole {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}) matches an
 * existing owner. Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both admitted &mdash; a
 * further household member is no longer rejected as a duplicate. Instead the new member's standing is
 * bounded downstream by {@link CapMembershipLevel}, which caps their membership level to one above
 * the highest already held in the household.
 *
 * <p>Runs after the normalize steps (so telephone is E.164 and email lower-cased) and before
 * {@link BuildOwner}, so no owner is persisted on conflict. The {@code householdId} is derived
 * exactly as {@link AssignHouseholdId} will later stamp it.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = OwnerIdentity.householdId(request.getLastName(), request.getPostcode());
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a new registration
            }
            String existingKey = OwnerIdentity.key(existing.getTelephone(), existing.getEmail(),
                    existing.getHouseholdId());
            if (identityKey.equals(existingKey)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
