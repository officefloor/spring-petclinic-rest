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
 * Runs after {@link BuildOwner}. When the request opts in with {@code sharesHousehold: true}, assigns
 * the new owner a {@code householdId} shared by every owner with the same last name (compared
 * case-insensitively with collapsed whitespace) and the same {@link AddressNormalizer normalized}
 * address, and back-fills that same id onto the existing household members, so both sides carry it. The
 * id is derived deterministically from the canonical last name and the normalized address, so it is
 * stable across requests. Owners that do not opt in keep a null householdId.
 */
public class AssignOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = canonical(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        String householdId = householdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(canonical(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))
                    && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** Stable, shared household id: the first 16 upper-hex chars of SHA-256 over the canonical
     *  last name and address, so all members of a household derive the same value. */
    private static String householdId(String lastName, String address) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "\n" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Lower-cased, trimmed, with any run of whitespace collapsed to a single space. */
    private static String canonical(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
