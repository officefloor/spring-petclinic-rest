package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects a create-owner request that shares an existing owner's last name and address,
 * compared case-insensitively with collapsed whitespace, with a 409 Conflict — unless the
 * request opted in with {@code sharesHousehold} true. Runs after {@link BuildOwner}.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold,
            OwnerRepository ownerRepository) throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return; // explicitly allowed to share a household
        }
        String lastName = key(owner.getLastName());
        String address = key(owner.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save), not a conflict
            }
            if (lastName.equals(key(existing.getLastName()))
                    && address.equals(key(existing.getAddress()))) {
                throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
            }
        }
    }

    /** Comparison key: lower-cased, with leading/trailing and repeated whitespace collapsed. */
    private static String key(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
