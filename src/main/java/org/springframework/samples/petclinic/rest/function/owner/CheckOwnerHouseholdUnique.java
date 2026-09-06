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
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;

/**
 * Enforces the household rule on create. An owner sharing an existing owner's last name and
 * address (compared case-insensitively with collapsed whitespace) is a 409 Conflict — unless the
 * request opted in with {@code sharesHousehold} true. When it did opt in, the two are allowed to
 * share a household and are given the same stable {@code householdId}: it is stamped on the new
 * owner and on every existing member of that household. Runs after {@link BuildOwner}.
 */
public class CheckOwnerHouseholdUnique {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold,
            OwnerRepository ownerRepository) throws DuplicateHouseholdException {
        String lastName = key(owner.getLastName());
        String address = key(owner.getAddress());
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save), not a conflict
            }
            if (lastName.equals(key(existing.getLastName()))
                    && address.equals(key(existing.getAddress()))) {
                household.add(existing);
            }
        }
        if (household.isEmpty()) {
            return; // no existing owner at this last name + address
        }
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            throw new DuplicateHouseholdException(owner.getLastName(), owner.getAddress());
        }
        // Explicitly allowed to share: assign the same stable household identifier to all members.
        String householdId = householdId(lastName, address, household);
        owner.setHouseholdId(householdId);
        for (Owner member : household) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                ownerRepository.save(member);
            }
        }
    }

    /**
     * The household's stable shared identifier: reuse the id an existing member already carries
     * (so a household keeps one value over time); otherwise derive it deterministically from the
     * normalized last name and address, so independent joiners compute the same value.
     */
    private static String householdId(String lastName, String address, List<Owner> household) {
        for (Owner member : household) {
            String existing = member.getHouseholdId();
            if (existing != null && !existing.isBlank()) {
                return existing;
            }
        }
        return "H-" + sha256Hex(lastName + "\n" + address).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Comparison key: lower-cased, with leading/trailing and repeated whitespace collapsed. */
    private static String key(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
