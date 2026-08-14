package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request whose last name and address are already used together by another
 * owner, so the escalation handler can respond 409. Last name is compared case-insensitively with
 * runs of whitespace collapsed to a single space; the address is compared in its normalized form
 * (see {@link AddressNormalizer}), so {@code "Smith"} at {@code "1  Main  St"} collides with
 * {@code "smith"} at {@code "1 Main Street"}.
 *
 * <p>Skipped when the request sets {@code sharesHousehold} true: the caller has confirmed the two
 * owners intentionally share a household, so the duplicate is allowed. Runs within the same write
 * transaction as the insert so the check and the insert see one consistent view.
 */
public class CheckHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = AddressNormalizer.normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
            }
        }
    }

    /** Lower-cases and collapses runs of whitespace to a single space, trimmed. Used for last name. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
