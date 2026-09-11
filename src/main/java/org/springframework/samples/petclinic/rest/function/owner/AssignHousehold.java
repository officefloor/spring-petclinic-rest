package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When the request opts into sharing a household ({@code sharesHousehold} true), assigns a
 * stable {@code householdId} derived from the normalized last name and address, so every
 * owner living together at the same address shares one identifier. Existing owners of the
 * same household that do not yet carry an id are back-filled with the same value, so both
 * the joining owner and the owner already at that address end up sharing it. Normalization
 * matches {@link RequireUniqueHousehold} (lower-cased, trimmed, whitespace collapsed).
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        String householdId = hash(lastName + "|" + address);
        owner.setHouseholdId(householdId);
        // Back-fill existing household members so both sides share the identifier.
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getHouseholdId() == null
                    && lastName.equals(normalize(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** Lower-case, trim, and collapse runs of whitespace to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** Stable 16-hex-character (upper-case) prefix of the SHA-256 of the household key. */
    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
