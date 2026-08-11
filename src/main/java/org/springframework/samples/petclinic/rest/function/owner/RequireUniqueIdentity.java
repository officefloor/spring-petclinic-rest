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
 * <p>Two independent conflicts are checked against every existing owner:
 * <ul>
 * <li><b>Exact identity</b>: the whole {@code identityKey}
 *   ({@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}) matches.
 *   This always conflicts &mdash; a genuinely identical record is never allowed.</li>
 * <li><b>Household duplicate</b>: the computed {@code householdId} matches, i.e. the same last name
 *   and postcode. This is rejected <em>unless</em> the request opts in with
 *   {@code sharesHousehold: true}, in which case the new owner is admitted as a declared member of
 *   the household.</li>
 * </ul>
 *
 * <p>Runs after the normalize steps (so telephone is E.164 and email lower-cased) and before
 * {@link BuildOwner}, so no owner is persisted on conflict. The {@code householdId} is derived
 * exactly as {@link AssignHouseholdId} will later stamp it.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = OwnerIdentity.householdId(request.getLastName(), request.getPostcode());
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
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
            if (!sharesHousehold && householdId != null
                    && householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
