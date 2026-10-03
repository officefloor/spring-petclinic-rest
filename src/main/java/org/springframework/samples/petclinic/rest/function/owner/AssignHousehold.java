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
 * When the request sets {@code sharesHousehold} true, joins the new owner to the
 * household formed by owners that share the same last name and address, using the
 * shared comparison in {@link Households} so it cannot drift from the uniqueness check
 * that {@link EnsureUniqueHousehold} applied.
 *
 * <p>Every owner in a household carries the same {@code householdId} — a stable
 * identifier derived from the normalized last name and address, so all members
 * compute the same value independently. The new owner receives it, and any existing
 * household members that do not yet carry it are back-filled so the whole household
 * shares one identifier.
 *
 * <p>Does nothing when {@code sharesHousehold} is absent or false, leaving a solo
 * owner's {@code householdId} null.
 */
public class AssignHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String lastName = Households.normalize(owner.getLastName());
        String address = Households.normalize(owner.getAddress());
        String householdId = deriveHouseholdId(lastName, address);
        owner.setHouseholdId(householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (Households.matches(existing, lastName, address)
                    && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** Stable identifier for a household: the first 12 upper-case hex characters of the
     *  SHA-256 of the normalized last name and address joined by a NUL separator. */
    private static String deriveHouseholdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "\u0000" + address).getBytes(StandardCharsets.UTF_8));
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
