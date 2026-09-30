package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects creating an owner who shares a household with an existing owner: same lastName
 * AND same address, each compared case-insensitively with collapsed whitespace. A
 * collision is rejected 409 via {@link DuplicateHouseholdException}, unless the request
 * opts in with {@code sharesHousehold=true}, in which case the check is bypassed.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (sameHousehold(request.getLastName(), request.getAddress(),
                    existing.getLastName(), existing.getAddress())) {
                throw new DuplicateHouseholdException(
                        "An owner with the same last name and address already exists");
            }
        }
    }

    /**
     * The single source of truth for what "same household" means: identical lastName AND address.
     * The lastName is compared case-insensitively with internal whitespace collapsed; the address is
     * compared on its normalized form (see {@link NormalizeOwnerAddress#normalize}) so abbreviation,
     * case and whitespace differences never split one household. Shared with
     * {@link AssignOwnerHousehold} so the 409 bypass and the {@code householdId} assignment can
     * never drift on what counts as one household.
     */
    static boolean sameHousehold(String lastNameA, String addressA, String lastNameB, String addressB) {
        return normalize(lastNameA).equals(normalize(lastNameB))
                && NormalizeOwnerAddress.normalize(addressA).equals(NormalizeOwnerAddress.normalize(addressB));
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
