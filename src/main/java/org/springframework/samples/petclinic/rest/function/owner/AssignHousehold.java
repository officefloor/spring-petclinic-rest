package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * When a create request opts in with {@code sharesHousehold} true, joins the new owner to
 * the household identified by its lastName (compared case-insensitively with collapsed
 * whitespace) and address (compared in its normalized form; see
 * {@link AddressNormalizer}). The household's
 * stable shared {@code householdId} is derived deterministically from that
 * lastName/address, so every member computes the same value. The new owner and every already-stored member of
 * the household are assigned that identifier, so both sides of a join share it.
 *
 * <p>Runs after {@code build}, so the new Owner entity exists but is not yet saved (and so
 * is not among the existing members backfilled here). A request that does not set
 * {@code sharesHousehold} leaves {@code householdId} null.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        String householdId = householdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))
                    && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** Stable 12-hex-char uppercase identifier for a household, derived from its normalized
     *  lastName and address so every member of the same household computes the same value. */
    private static String householdId(String lastName, String address) {
        return sha256hex(lastName + "\n" + address).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Case-insensitive with collapsed whitespace: trim, fold internal whitespace runs to a
     *  single space, and lower-case. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
