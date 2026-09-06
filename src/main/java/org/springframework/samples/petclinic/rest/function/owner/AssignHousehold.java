package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * When the request opted into a shared household ({@code sharesHousehold} true), assigns the new
 * owner a stable {@code householdId} shared by everyone at the same last name and address (the last
 * name compared case-insensitively with runs of whitespace collapsed, and the address in its
 * normalized form, matching {@link EnsureUniqueHousehold}).
 *
 * <p>The identifier is derived deterministically from the normalized last name and address, so every
 * owner in the household resolves to the same value even across separate requests. If an existing
 * household member already carries an id, that value is reused for stability; any members still
 * missing it are back-filled, so both ends of the shared household carry the same id.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = Addresses.normalize(owner.getAddress());
        List<Owner> members = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> normalize(existing.getLastName()).equals(lastName)
                        && Addresses.normalize(existing.getAddress()).equals(address))
                .toList();
        // Reuse an id already held by a household member for stability; otherwise derive one.
        String householdId = members.stream()
                .map(Owner::getHouseholdId)
                .filter(id -> id != null && !id.isBlank())
                .findFirst()
                .orElseGet(() -> deriveHouseholdId(lastName, address));
        owner.setHouseholdId(householdId);
        for (Owner member : members) {
            if (member.getHouseholdId() == null || member.getHouseholdId().isBlank()) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }

    private static String deriveHouseholdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("HH-");
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
