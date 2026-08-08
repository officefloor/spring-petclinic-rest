package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The consolidated duplicate check, run after {@link AssignOwnerHousehold} (so the
 * deterministic {@code householdId} is set) and before {@link SaveOwner}. It rejects a
 * new owner as a 409 Conflict in two cases:
 *
 * <ul>
 * <li><b>Household duplicate</b> — the new owner's computed {@code householdId} matches an
 * existing owner's, i.e. it shares that owner's {@code lastName} and {@code postcode} and
 * is therefore the same household. Such a create is rejected <em>unless</em> it opted in
 * with {@code sharesHousehold}, in which case it is admitted as a declared additional
 * member of that household.</li>
 * <li><b>Exact identity duplicate</b> — the whole {@code identityKey} (see
 * {@link OwnerIdentity}) equals an existing owner's. This still applies to owners with no
 * household (no postcode), for whom the household rule cannot fire, and is never bypassed
 * by {@code sharesHousehold}: an identical owner is a hard duplicate, not a new member.</li>
 * </ul>
 *
 * <p>The {@code sharesHousehold} flag therefore no longer creates the household link (the
 * id is now derived purely from lastName+postcode); it only lets a genuine second member
 * through the duplicate block.
 */
public class RejectDuplicateOwnerIdentity {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        String identityKey = OwnerIdentity.identityKey(owner);
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never conflict with the owner itself
            }
            if (identityKey.equals(OwnerIdentity.identityKey(existing))) {
                throw new DuplicateOwnerIdentityException(
                        "An owner with the same identity already exists");
            }
            if (!sharesHousehold && householdId != null
                    && householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateOwnerIdentityException(
                        "An owner in the same household already exists");
            }
        }
    }
}
