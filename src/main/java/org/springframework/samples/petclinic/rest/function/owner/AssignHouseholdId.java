package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * When the request opts in with {@code sharesHousehold} true, gives the new owner a stable
 * {@code householdId} shared with the existing owner(s) at the same household — same last name and
 * same address, compared case-insensitively with collapsed whitespace, matching
 * {@link EnsureOwnerHouseholdUnique}.
 *
 * <p>The identifier is deterministic (a hex prefix of SHA-256 over the normalized last name and
 * address), so every member of a household derives the same value and it stays stable as new members
 * join. Any pre-existing household member that has no identifier yet is back-filled with the same
 * value so the whole household shares it. Runs after {@link BuildOwner} (so the entity exists) and
 * before {@link SaveOwner}, mutating the not-yet-persisted owner in place.
 */
public class AssignHouseholdId {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());

        List<Owner> household = new ArrayList<>();
        String existingId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                household.add(existing);
                if (existingId == null && existing.getHouseholdId() != null
                        && !existing.getHouseholdId().isBlank()) {
                    existingId = existing.getHouseholdId();
                }
            }
        }

        String householdId = existingId != null ? existingId : stableId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner member : household) {
            if (member.getHouseholdId() == null || member.getHouseholdId().isBlank()) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }

    /** Trim, collapse internal whitespace to a single space and lower-case for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** First 12 upper-case hex characters of SHA-256 over the normalized last name and address. */
    private static String stableId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12).toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
