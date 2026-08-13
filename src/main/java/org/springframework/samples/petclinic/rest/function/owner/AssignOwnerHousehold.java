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
 * Runs on {@code POST /api/owners} after {@link BuildOwner}, when the request opts in with
 * {@code sharesHousehold} true. When another owner already shares the household — same last name
 * and same address, compared case-insensitively with collapsed whitespace (the same comparison
 * {@link CheckUniqueOwnerHousehold} skips) — every member of that household is given one stable
 * shared {@code householdId}: an existing member's id is reused if present, otherwise a new id is
 * derived deterministically from the normalized last name and address (so independent joiners
 * compute the same value). The new owner and any member lacking an id are assigned it, and the
 * existing members are saved so the whole household shares the identifier. Without a matching
 * owner, or when {@code sharesHousehold} is not set, no household is formed and no id is assigned.
 */
public class AssignOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(request.getLastName());
        String address = normalize(request.getAddress());
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))) {
                household.add(existing);
            }
        }
        if (household.isEmpty()) {
            return;
        }
        String householdId = existingHouseholdId(household);
        if (householdId == null) {
            householdId = deriveHouseholdId(lastName, address);
        }
        owner.setHouseholdId(householdId);
        for (Owner member : household) {
            if (member.getHouseholdId() == null) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }

    /** Reuse an id already assigned to any household member, so late joiners keep the shared value. */
    private static String existingHouseholdId(List<Owner> household) {
        for (Owner member : household) {
            if (member.getHouseholdId() != null) {
                return member.getHouseholdId();
            }
        }
        return null;
    }

    /** A stable 8-hex-character identifier derived from the normalized last name and address. */
    private static String deriveHouseholdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Lower-case and collapse runs of whitespace so comparison is case- and whitespace-insensitive. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
