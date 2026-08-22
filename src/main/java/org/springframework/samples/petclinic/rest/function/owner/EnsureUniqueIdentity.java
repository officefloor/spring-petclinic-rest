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
 * with {@code sharesHousehold} (created as a declared household member) or the new owner carries a
 * distinct email — an email that no colliding household member shares — which identifies it as a
 * separate person joining the household rather than a re-registration.</li>
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
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer blocks a create
            }
            if (identityKey.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
            if (!sharesHousehold && householdId != null
                    && householdId.equals(existing.getHouseholdId())
                    && !distinctEmail(owner, existing)) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }

    /** Whether the new owner carries a non-blank email that the colliding household member does not
     *  share (compared case-insensitively) — a personal identifier marking it a separate person. */
    private static boolean distinctEmail(Owner owner, Owner existing) {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return false;
        }
        return !email.equalsIgnoreCase(existing.getEmail());
    }
}
