package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Rejects creating an owner whose household — the same last name and the same address — is
 * already used by another (existing) owner. Last name and address are compared
 * case-insensitively with runs of whitespace collapsed to a single space (and trimmed), so
 * e.g. "110  W.  Liberty St." matches "110 w. liberty st.". A match on a different owner
 * escalates as a 409 via {@link DuplicateHouseholdException}.
 *
 * <p>The check is skipped when the request set {@code sharesHousehold} true (published by
 * {@link BuildOwner}): such owners are allowed to share a household.
 */
public class RejectDuplicateHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                throw new DuplicateHouseholdException(
                        "Another owner already shares this household (same last name and address)");
            }
        }
    }

    /** Lower-case, trim, and collapse any run of whitespace to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
