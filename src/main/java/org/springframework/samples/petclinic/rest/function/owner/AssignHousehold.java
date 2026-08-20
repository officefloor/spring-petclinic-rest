package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Joins a deliberately-shared household. When the create request opts in via
 * {@code sharesHousehold} and an existing owner has the same last name and address (the address in
 * its normalized form and the last name case-insensitively with whitespace collapsed), the new
 * owner and the existing household member(s) are given the same, stable
 * {@code householdId}. If a matching owner already carries a household id it is reused; otherwise a
 * new id is derived from the household's normalized last name and address, so the same household
 * always yields the same value. Does nothing when the request does not opt in or no matching owner
 * exists — a lone owner has no household to share.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalizeName(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        List<Owner> household = new ArrayList<>();
        String existingId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalizeName(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                household.add(existing);
                if (existingId == null && existing.getHouseholdId() != null) {
                    existingId = existing.getHouseholdId();
                }
            }
        }
        if (household.isEmpty()) {
            return; // nobody to share a household with
        }
        String householdId = existingId != null ? existingId : deriveHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner member : household) {
            if (member.getHouseholdId() == null) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }

    /** A stable {@code HH-<12 upper hex>} id derived from the household's normalized identity. */
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
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /** Trim, collapse internal whitespace runs to a single space, and lower-case. */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
