package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region derived from the owner's postcode (falling back to the city table) and HASH8 is the first
 * eight upper-case hex characters of SHA-256 over the normalized telephone concatenated with the
 * last name (e.g. {@code NSW-1A2B3C4D}). There is no per-city sequence number. Runs after telephone
 * normalization so the hash is taken over the stored, normalized (E.164) telephone.
 *
 * <p>If the computed code collides with an existing owner's {@code customerCode}, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique, so distinct owners always
 * receive distinct customer codes. Runs before the owner is saved, so it is matched only against
 * owners already present.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Localities.region(owner.getPostcode(), owner.getCity());
        String hash8 = shaHex(owner.getTelephone() + owner.getLastName(), 8);
        String base = region + "-" + hash8;

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            existing.add(other.getCustomerCode());
        }

        String code = base;
        for (int n = 2; existing.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }

    /** First {@code length} upper-case hex characters of SHA-256({@code value}). */
    private static String shaHex(String value, int length) {
        return sha256hex(value).substring(0, length).toUpperCase();
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
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
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
