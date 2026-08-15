package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.function.common.IdentityKeys;

/**
 * The duplicate-detection step of {@code POST /api/owners}. Runs after {@link ValidateOwnerFields}
 * has normalized the telephone and email. It computes the request's deterministic
 * {@code householdId} from its last name and postcode (see {@link Households#householdId}) and
 * rejects the create with a 409 Conflict in two cases:
 *
 * <ul>
 * <li><b>Identity duplicate</b> — the request's whole {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}) equals an existing
 * owner's. This always rejects, even when {@code sharesHousehold} is set.</li>
 * <li><b>Household duplicate</b> — an existing owner already occupies the same household (same
 * computed {@code householdId}, i.e. same last name and postcode). This rejects <em>unless</em> the
 * request opts in with {@code sharesHousehold: true}, in which case the owner is created as a
 * declared household member.</li>
 * </ul>
 *
 * <p>{@code sharesHousehold} therefore only bypasses the household-duplicate block; it never lets a
 * full identity duplicate through.
 */
public class RejectDuplicateIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String householdId = Households.householdId(request.getLastName(), request.getPostcode());
        String identityKey = IdentityKeys.of(request.getTelephone(), request.getEmail(), householdId);
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(IdentityKeys.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
            if (!sharesHousehold && householdId.equals(householdIdOf(existing))) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }

    /** The household an existing owner belongs to, computed from its last name and postcode. */
    private static String householdIdOf(Owner existing) {
        return Households.householdId(existing.getLastName(), existing.getPostcode());
    }
}
