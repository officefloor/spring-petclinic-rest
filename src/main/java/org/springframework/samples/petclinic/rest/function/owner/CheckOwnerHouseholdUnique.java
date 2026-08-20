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
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerHouseholdException;

/**
 * Governs the household rule for a create-owner request. A "household" is a set of owners
 * that share the same last name and the same address, both compared case-insensitively with
 * runs of whitespace collapsed to a single space and leading/trailing whitespace trimmed.
 *
 * <p>When the request would join an existing household (a match is found):
 * <ul>
 *   <li>If the request did not set {@code sharesHousehold} true, it is rejected with a 409.</li>
 *   <li>If it set {@code sharesHousehold} true, the join is allowed and a stable shared
 *       {@code householdId} — derived from the canonical last name and address, so every
 *       member of the household derives the same value — is stamped onto the existing
 *       members and published for {@link AssignOwnerHousehold} to stamp onto the new owner.</li>
 * </ul>
 *
 * <p>When there is no existing household match the request is a plain unique owner and no
 * {@code householdId} is assigned.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository,
            Out<HouseholdId> householdId) throws DuplicateOwnerHouseholdException {
        String lastName = canonical(request.getLastName());
        String address = canonical(request.getAddress());
        List<Owner> members = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(canonical(existing.getLastName()))
                    && address.equals(canonical(existing.getAddress()))) {
                members.add(existing);
            }
        }
        if (members.isEmpty()) {
            return; // no existing household — a plain unique owner, no householdId
        }
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            throw new DuplicateOwnerHouseholdException(request.getLastName(), request.getAddress());
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
