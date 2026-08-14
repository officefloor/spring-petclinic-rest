package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Runs in the create-owner pipeline before {@link BuildOwner}. Compares the request's lastName and
 * address against every existing owner — case-insensitively with collapsed whitespace. When they
 * match an existing owner and the request did NOT set {@code sharesHousehold} true, the request is
 * rejected with a 409 so the same household is not registered twice.
 *
 * <p>When {@code sharesHousehold} is true, a match is instead permitted: this step derives a stable
 * shared {@code householdId} from the canonical lastName and address, stamps it onto the matching
 * existing owner(s) that lack one, and publishes it as an {@link Out} so {@link AssignHousehold}
 * can stamp the same value onto the owner being created. Owners in the same household therefore
 * carry an identical, stable identifier. A request with no matching owner publishes {@code null}
 * (there is no household to share).
 */
public class EnsureUniqueHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository,
            Out<HouseholdId> householdIdOut) throws DuplicateHouseholdException {
        String lastName = canonical(request.getLastName());
        String address = canonical(request.getAddress());

        List<Owner> matches = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(canonical(existing.getLastName()))
                    && address.equals(canonical(existing.getAddress()))) {
                matches.add(existing);
            }
        }

        if (matches.isEmpty()) {
            householdIdOut.set(null); // unique household — nothing to share
            return;
        }

        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            throw new DuplicateHouseholdException(request.getLastName(), request.getAddress());
        }

        // Caller opted into sharing: give this owner and every match the same stable identifier.
        String householdId = deriveHouseholdId(lastName, address);
        for (Owner match : matches) {
            if (match.getHouseholdId() == null) {
                match.setHouseholdId(householdId);
                ownerRepository.save(match);
            }
        }
        householdIdOut.set(new HouseholdId(householdId));
    }

    /** Deterministic, stable identifier for a household — same lastName + address always yields it. */
    private static String deriveHouseholdId(String canonicalLastName, String canonicalAddress) {
        String seed = "household:" + canonicalLastName + "|" + canonicalAddress;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /** Trim, collapse internal whitespace runs to a single space and lower-case for comparison. */
    private static String canonical(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
