package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerHouseholdException;

/**
 * Rejects a new owner that would share a household with an existing owner: same
 * {@code lastName} and same {@code address}. Addresses are compared in their normalized
 * form (see {@link AddressNormalizer}); last names are compared case-insensitively after
 * collapsing runs of whitespace to a single space. Throws a checked
 * {@link DuplicateOwnerHouseholdException} (turned into a 409 Conflict by the
 * escalation handler) unless the request opted in with {@code sharesHousehold} true.
 */
public class RejectDuplicateOwnerHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request,
            OwnerRepository ownerRepository) throws DuplicateOwnerHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // explicitly allowed to share a household
        }
        String lastName = normalize(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        if (lastName == null || address == null) {
            return; // nothing to compare against
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never conflict with the owner itself
            }
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                throw new DuplicateOwnerHouseholdException(
                        "Another owner with the same last name and address already exists");
            }
        }
    }

    /** Last-name key: lower-cased, trimmed, with internal whitespace runs collapsed to one space. */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String collapsed = value.trim().replaceAll("\\s+", " ");
        return collapsed.isEmpty() ? null : collapsed.toLowerCase();
    }
}
