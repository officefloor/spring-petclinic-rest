package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns a shared {@code householdId} when a create request opts in with
 * {@code sharesHousehold: true}, joining an existing owner at the same household — the
 * same {@code lastName} (compared case-insensitively with collapsed whitespace) and the
 * same {@code address} in its normalized form (see {@link AddressNormalizer}), matching
 * {@link EnsureUniqueHousehold}.
 *
 * <p>The identifier is derived deterministically from the normalized last name and
 * address, so every owner of a household computes the same value regardless of creation
 * order — a stable shared identifier. Existing same-household owners that do not yet
 * carry one are back-filled so both sides share it.
 *
 * <p>Runs after {@link BuildOwner} (which produces the {@link Owner}) and before
 * {@link SaveOwner}; it mutates the built owner in place (see {@code @Val} semantics).
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String address = normalizeAddress(owner.getAddress());
        String householdId = householdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && address.equals(normalizeAddress(existing.getAddress()))
                    && existing.getHouseholdId() == null) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** Stable 16-char upper-case hex identifier over the normalized last name and address. */
    private static String householdId(String lastName, String address) {
        return sha256hex(lastName + "|" + address).substring(0, 16).toUpperCase();
    }

    private static String sha256hex(String value) {
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
            throw new IllegalStateException(e);
        }
    }

    /** Lower-cases and collapses runs of whitespace to a single space, trimming the ends. */
    private static String normalize(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase();
    }

    /** The canonical address form (see {@link AddressNormalizer}); never {@code null}. */
    private static String normalizeAddress(String value) {
        String normalized = AddressNormalizer.normalize(value);
        return normalized == null ? "" : normalized;
    }
}
