package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time uniqueness guard for {@code POST /owners}. It rejects a request with a 409 via
 * {@link DuplicateIdentityException} in two cases:
 *
 * <ul>
 *   <li><b>Household duplicate.</b> A household is keyed on {@code (lastName, postcode)} (see
 *       {@link OwnerHousehold}), so an owner sharing an existing owner's last name and postcode is
 *       the same household and is rejected — <em>unless</em> the request sets {@code sharesHousehold},
 *       which bypasses this block so the owner is created as a declared household member.</li>
 *   <li><b>Exact identity duplicate.</b> A request whose whole {@link OwnerIdentity identityKey}
 *       (normalised telephone, email and household together) equals an existing owner's is rejected
 *       regardless of {@code sharesHousehold} — a genuinely identical owner is always a duplicate.</li>
 * </ul>
 *
 * <p>The household id is derived from {@code (lastName, postcode)} for both the request and each
 * existing owner, so the comparison is independent of any stored id. Runs after
 * {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} (so the request's telephone and
 * email are in their stored, canonical forms) and before {@link BuildOwner}.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        String householdId = OwnerHousehold.deriveId(request);
        String identityKey = OwnerIdentity.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is not a duplicate
            }
            if (!sharesHousehold && householdId != null
                    && householdId.equals(OwnerHousehold.deriveId(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
