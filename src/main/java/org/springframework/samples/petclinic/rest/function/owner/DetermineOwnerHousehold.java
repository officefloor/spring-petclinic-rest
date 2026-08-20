package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the shared {@code householdId} for a create-owner request. A "household" is a set of
 * owners that share the same last name and the same address. The last name is compared
 * case-insensitively with runs of whitespace collapsed to a single space and leading/trailing
 * whitespace trimmed; the address is compared in its {@link AddressNormalizer normalized} form.
 *
 * <p>When the request opts in with {@code sharesHousehold} true and matches an existing household,
 * the join is allowed and a stable shared {@code householdId} — derived from the canonical last
 * name and address, so every member of the household derives the same value — is stamped onto the
 * existing members and published for {@link AssignOwnerHousehold} to stamp onto the new owner.
 *
 * <p>Otherwise (no opt-in, or no existing household match) no {@code householdId} is assigned and
 * the owner remains outside any household. Duplicate detection is no longer performed here: it is
 * handled uniformly by {@link CheckOwnerIdentityUnique} on the whole {@link OwnerIdentityKey
 * identity key}, of which the resolved {@code householdId} is one part.
 */
public class DetermineOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository,
            Out<HouseholdId> householdId) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // did not opt into a shared household — no householdId
        }
        String lastName = canonical(request.getLastName());
        String address = AddressNormalizer.normalize(request.getAddress());
        List<Owner> members = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(canonical(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                members.add(existing);
            }
        }
        if (members.isEmpty()) {
            return; // no existing household to join
        }
        // Opted in to share the household: assign the same stable id to every member.
        String id = deriveHouseholdId(lastName, address);
        for (Owner member : members) {
            member.setHouseholdId(id);
            ownerRepository.save(member);
        }
        householdId.set(new HouseholdId(id));
    }

    /**
     * A stable household identifier derived from the canonical last name and address, so that
     * every owner joining the same household independently derives an identical value.
     */
    static String deriveHouseholdId(String canonicalLastName, String canonicalAddress) {
        String seed = canonicalLastName + "\n" + canonicalAddress;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return "HH-" + hex;
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static String canonical(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
