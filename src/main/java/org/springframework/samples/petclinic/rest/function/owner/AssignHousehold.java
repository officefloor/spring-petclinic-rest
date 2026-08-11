package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns a shared {@code householdId} when a create-owner request opts in with
 * {@code sharesHousehold=true} and there is an existing owner with the same last name and
 * address (the address compared in its normalized form via {@link AddressNormalizer} — the
 * the same normalized last-name-and-address rule). The identifier is <em>stable</em>: it is derived
 * deterministically from the normalized last name and address, so every housemate computes
 * the same value regardless of creation order. The value is set on the newly built
 * {@link Owner} and back-filled onto the existing housemate(s) so both carry it.
 *
 * <p>Runs after {@link BuildOwner} (so the entity exists) and before {@link SaveOwner}; when
 * the request does not opt in, or opts in but has no existing household to join, it is a no-op.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner built, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = AddressNormalizer.normalize(request.getAddress());
        List<Owner> housemates = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                housemates.add(existing);
            }
        }
        if (housemates.isEmpty()) {
            return;
        }
        String householdId = householdId(lastName, address);
        built.setHouseholdId(householdId);
        for (Owner mate : housemates) {
            if (!householdId.equals(mate.getHouseholdId())) {
                mate.setHouseholdId(householdId);
                ownerRepository.save(mate);
            }
        }
    }

    /** A stable household identifier: first 12 upper-case hex chars of SHA-256 over the
     *  normalized last name and address, so all housemates derive the same value. */
    private static String householdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12).toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Lower-case, trim, and collapse internal whitespace runs to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
