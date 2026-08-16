package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects creating an owner that duplicates an existing one, so the create endpoint responds 409
 * instead of storing a duplicate. Two rules, both keyed off the deterministic household:
 *
 * <ul>
 *   <li><strong>Exact identity duplicate</strong> — the new owner's whole identity key
 *       ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, see
 *       {@link OwnerIdentity}) equals an existing owner's. This is genuinely the same person and is
 *       always rejected.</li>
 *   <li><strong>Household duplicate</strong> — the new owner shares an existing owner's household,
 *       i.e. the same {@code householdId} computed from {@code (lastName, postcode)}. Because the
 *       household is now keyed on {@code (lastName, postcode)}, a second owner with the same last name
 *       and postcode is the same household and is rejected — <em>unless</em> the request opts in with
 *       {@code sharesHousehold: true}, which bypasses this block so the owner is created as a declared
 *       household member.</li>
 * </ul>
 *
 * <p>An owner without a postcode has no household ({@code householdId} is null), so only the exact
 * identity rule applies to it.
 *
 * <p>Runs after {@link ValidateOwner} (which normalizes and publishes the body) and before
 * {@link BuildOwner}, comparing against every existing owner.
 */
public class CheckIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = OwnerIdentity.householdIdFor(request.getLastName(), request.getPostcode());
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), householdId);
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        for (Owner existing : ownerRepository.findAll()) {
            // A soft-deleted owner is not a live duplicate: skip it so its identity/household frees up.
            if (existing.isDeleted()) {
                continue;
            }
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
            if (!sharesHousehold && householdId != null && householdId.equals(
                    OwnerIdentity.householdIdFor(existing.getLastName(), existing.getPostcode()))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
