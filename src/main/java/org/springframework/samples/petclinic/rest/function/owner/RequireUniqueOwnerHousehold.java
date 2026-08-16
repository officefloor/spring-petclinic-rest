package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerHouseholdDuplicateException;

/**
 * Rejects the create when another owner already belongs to this owner's household — the same
 * computed {@link AssignHouseholdId#deriveHouseholdId householdId}, which is derived from the
 * (lastName, postcode) pair. Owners sharing a lastName and postcode are the same household, so a
 * second such owner is rejected 409 via {@link OwnerHouseholdDuplicateException}.
 *
 * <p>Bypassed when the request sets {@code sharesHousehold} true — the caller has confirmed the
 * two owners knowingly live together. Such a declared member is then created (and, sharing the
 * computed householdId, becomes part of the household) rather than blocked.
 *
 * <p>Runs after {@link AssignHouseholdId}, so the owner already carries its computed
 * {@code householdId} and every existing member carries the same value.
 */
public class RequireUniqueOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) throws OwnerHouseholdDuplicateException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // never match the new owner against itself
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new OwnerHouseholdDuplicateException(request.getLastName(),
                        request.getAddress());
            }
        }
    }

    /** Lower-case, trimmed, with internal whitespace runs collapsed to a single space. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
