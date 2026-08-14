package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners} that assigns a shared {@code householdId} when the request opts
 * into an existing household via {@code sharesHousehold}.
 *
 * <p>When {@code sharesHousehold} is true and an existing owner already shares the same last name
 * and address (last name compared case-insensitively after trimming and collapsing whitespace, and
 * address by its canonical normalized form, matching {@link EnsureUniqueHousehold}), a stable
 * identifier derived from that normalized last name and address is assigned to the new owner and
 * back-filled onto every matching existing owner. Because
 * the identifier is derived deterministically from the household key, all owners of a household share
 * the same value regardless of creation order. Runs after {@link BuildOwner}, so it works on the
 * built entity, and before {@link SaveOwner}.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = AddressNormalizer.normalize(request.getAddress());
        String householdId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (normalize(existing.getLastName()).equals(lastName)
                    && AddressNormalizer.normalize(existing.getAddress()).equals(address)) {
                if (householdId == null) {
                    householdId = householdId(lastName, address);
                }
                if (!householdId.equals(existing.getHouseholdId())) {
                    existing.setHouseholdId(householdId);
                    ownerRepository.save(existing);
                }
            }
        }
        if (householdId != null) {
            owner.setHouseholdId(householdId);
        }
    }

    private static String householdId(String lastName, String address) {
        String key = lastName + "|" + address;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("H-");
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
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
