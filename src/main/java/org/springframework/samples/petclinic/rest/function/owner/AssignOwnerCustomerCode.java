package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode} on create, formatted {@code <REGION>-<HASH8>}
 * where {@code REGION} is the region code derived from the postcode (see
 * {@link OwnerRegion}) and {@code HASH8} is the first 8 upper-case hex characters of
 * SHA-256 over ({@code normalizedTelephone + lastName}) (e.g. {@code NSW-1A2B3C4D}).
 *
 * <p>Runs after the telephone has been normalized to E.164, so the hash is stable over the
 * stored telephone. The base code is fully determined by the owner's region and the
 * telephone-plus-surname hash; should it collide with an existing owner's
 * {@code customerCode}, {@code -<n>} is appended with the smallest {@code n} of 2 or more
 * that makes it unique.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.regionOf(owner);
        String hash8 = sha256Hex8(owner.getTelephone() + owner.getLastName());
        String base = region + "-" + hash8;

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getId() != null && other.getId().equals(owner.getId())) {
                continue;
            }
            String code = other.getCustomerCode();
            if (code != null) {
                existing.add(code);
            }
        }

        String code = base;
        for (int n = 2; existing.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code s}. */
    private static String sha256Hex8(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) { // 4 bytes -> 8 hex characters
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
