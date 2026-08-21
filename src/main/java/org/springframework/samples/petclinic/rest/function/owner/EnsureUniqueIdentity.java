package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The consolidated duplicate check, rejecting a create with 409 in either of two ways:
 *
 * <ul>
 * <li><b>Full-identity duplicate</b> — the whole {@code identityKey} (see {@link OwnerIdentity},
 * {@code normalizedTelephone|email|householdId}) equals an existing owner's. Because the telephone
 * is part of the key, two members of the same household with different telephones have different
 * keys; only an exact whole-key match collides. This always applies, even when
 * {@code sharesHousehold} is set.</li>
 * <li><b>Household duplicate</b> — the new owner's computed {@code householdId} (keyed on
 * lastName + postcode; see {@link AssignHousehold}) already identifies an existing owner, i.e. a
 * second owner sharing lastName and postcode. This is blocked <em>unless</em> the request opts in
 * with {@code sharesHousehold}, in which case the new owner is created as a declared household
 * member.</li>
 * </ul>
 *
 * <p>Runs after {@link AssignHousehold} so the new owner's {@code householdId} is set before it is
 * compared. The new owner is not yet saved, so it is not among the existing owners compared here.
 */
public class EnsureUniqueIdentity {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = OwnerIdentity.of(owner);
        String householdId = owner.getHouseholdId();
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
            if (!sharesHousehold && householdId != null
                    && householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
