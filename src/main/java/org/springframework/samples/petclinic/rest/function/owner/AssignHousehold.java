package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns a shared {@code householdId} when the request opts in with {@code sharesHousehold: true}.
 *
 * <p>The identifier is derived deterministically from the normalized last name and address (the same
 * fields {@link CheckHouseholdUnique} compares), so it is stable: every owner living at the same
 * address under the same last name gets the same value regardless of creation order. The new owner and
 * every existing owner in that household are stamped with it, so both sides of an intentionally shared
 * household report the identifier. When {@code sharesHousehold} is not true no identifier is assigned.
 *
 * <p>Runs after {@link BuildOwner} (which publishes the new owner) and before {@link SaveOwner}.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        String householdId = householdId(lastName, address);
        owner.setHouseholdId(householdId);
        // Stamp existing members of the same household so both sides share the identifier.
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalize(existing.getAddress()))
                    && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** A stable, shared identifier: the first 16 upper-case hex chars of SHA-256(lastName|address). */
    private static String householdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 16).toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Lower-cases and collapses whitespace so comparison ignores case and spacing differences. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
