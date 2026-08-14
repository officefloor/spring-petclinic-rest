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

/**
 * Runs in the create-owner pipeline before {@link EnsureUniqueIdentity} and {@link BuildOwner}.
 * Resolves the shared {@code householdId} that forms part of the owner's {@code identityKey}.
 *
 * <p>When {@code sharesHousehold} is true and an existing owner matches on canonical lastName
 * (case-insensitive, collapsed whitespace) and {@link OwnerAddresses#normalize normalized} address,
 * this step derives a stable shared {@code householdId} from that canonical lastName and address,
 * stamps it onto the matching existing owner(s) that lack one, and publishes it as an {@link Out}
 * so {@link AssignHousehold} can stamp the same value onto the owner being created. Owners in the
 * same household therefore carry an identical, stable identifier.
 *
 * <p>Household membership is no longer a duplicate on its own: the previous household duplicate
 * check is now expressed through the single {@code identityKey}. A request with no matching owner,
 * or one that does not opt into sharing, publishes {@code null} — it has a unique household, so its
 * identityKey carries an empty household component and collides only on an exact full-key match.
 */
public class DeriveHouseholdId {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository,
            Out<HouseholdId> householdIdOut) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            householdIdOut.set(null); // not sharing — a household of one, no shared id
            return;
        }

        String lastName = canonical(request.getLastName());
        String address = OwnerAddresses.normalize(request.getAddress());

        List<Owner> matches = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(canonical(existing.getLastName()))
                    && address.equals(OwnerAddresses.normalize(existing.getAddress()))) {
                matches.add(existing);
            }
        }

        if (matches.isEmpty()) {
            householdIdOut.set(null); // no household to share yet
            return;
        }

        // Give this owner and every match the same stable identifier.
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
