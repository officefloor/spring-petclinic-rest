package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs after {@link BuildOwner} when the request set {@code sharesHousehold: true}: the new owner is
 * joining an existing household (another owner with the same last name at the same address, already
 * permitted by {@link CheckOwnerHouseholdUnique}). Assigns a stable, shared {@code householdId} to the
 * new owner and back-fills it onto the existing household member(s), so every owner in the household
 * reports the same identifier.
 *
 * <p>The identifier is derived deterministically from the normalized last name and address (compared
 * the same way as {@link CheckOwnerHouseholdUnique}), so it is stable across creations and identical
 * for everyone in one household regardless of the order they were added.
 */
public class AssignHouseholdId {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        String householdId = deriveHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        // Back-fill the shared id onto the existing household member(s) so both sides match.
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))
                    && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** Stable 12-character upper-case hex identifier derived from the household's name and address. */
    private static String deriveHouseholdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "\n" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
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
