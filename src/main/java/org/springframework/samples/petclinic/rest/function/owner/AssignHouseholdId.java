package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the {@code householdId} when the request opts into sharing a household
 * ({@code sharesHousehold} true). The identifier is a stable value derived deterministically
 * from the household identity — the {@code lastName} and {@code address} normalized the same
 * way {@link RequireUniqueOwnerHousehold} compares them (case-insensitive, whitespace
 * collapsed) — so every owner of the same household is assigned the same value regardless of
 * creation order.
 *
 * <p>Any existing owners already in that household are back-filled with the same identifier,
 * so both the joining owner and the owners already there carry it.
 *
 * <p>When {@code sharesHousehold} is not true, no identifier is assigned and the owner keeps
 * a null {@code householdId}.
 */
public class AssignHouseholdId {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = RequireUniqueOwnerHousehold.normalize(owner.getLastName());
        String address = RequireUniqueOwnerHousehold.normalize(owner.getAddress());
        String householdId = deriveHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (RequireUniqueOwnerHousehold.normalize(existing.getLastName()).equals(lastName)
                    && RequireUniqueOwnerHousehold.normalize(existing.getAddress()).equals(address)
                    && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** {@code HH-} followed by the first 16 upper-case hex chars of SHA-256(lastName|address). */
    static String deriveHouseholdId(String lastName, String address) {
        String key = lastName + "|" + address;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("HH-");
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
