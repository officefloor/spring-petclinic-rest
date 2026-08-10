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
 * Assigns a shared {@code householdId} to owners that intentionally share a household.
 *
 * <p>Runs after {@link BuildOwner} (which produces the new {@link Owner}) and only acts when the
 * request opted in with {@code sharesHousehold=true}. It finds every existing owner with the same
 * last name and address (last name case-insensitive with collapsed whitespace, address in its
 * normalized form per {@link OwnerAddress}, matching by last name and normalized address). When at
 * least one such owner exists — the
 * household this owner is joining — a stable identifier is chosen and stamped onto the new owner
 * and back-filled onto the existing members, so every owner in the household reports the same
 * {@code householdId}.
 *
 * <p>The identifier is derived deterministically from the normalized last name and address, so it
 * is the same value each joining owner computes regardless of ordering; an identifier already
 * present on an existing member is reused as-is.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalizeName(owner.getLastName());
        String address = OwnerAddress.normalizeForCompare(owner.getAddress());
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalizeName(existing.getLastName()))
                    && address.equals(OwnerAddress.normalizeForCompare(existing.getAddress()))) {
                household.add(existing);
            }
        }
        if (household.isEmpty()) {
            // sharesHousehold was set but no existing member shares this address; nothing to join.
            return;
        }
        String householdId = existingHouseholdId(household);
        if (householdId == null) {
            householdId = deriveHouseholdId(lastName, address);
        }
        owner.setHouseholdId(householdId);
        for (Owner member : household) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }

    private static String existingHouseholdId(List<Owner> household) {
        for (Owner member : household) {
            String id = member.getHouseholdId();
            if (id != null && !id.isBlank()) {
                return id;
            }
        }
        return null;
    }

    private static String deriveHouseholdId(String lastName, String address) {
        return "H-" + shaHex(lastName + "|" + address, 12);
    }

    private static String shaHex(String value, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, length).toUpperCase();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }

    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
