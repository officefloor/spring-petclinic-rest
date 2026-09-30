package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CityRegions;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerTelephones;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is
 * the region derived from the postcode by the shared {@link CityRegions} table (or {@code UNKNOWN}
 * when the postcode is absent or in no known range) and HASH8 is the first 8 upper-case hex
 * characters of SHA-256 over {@code normalizedTelephone + lastName} (e.g. {@code NSW-3A7F1C2D}).
 * The telephone is normalized to E.164 via {@link OwnerTelephones} so the hash is stable regardless
 * of the incoming format.
 *
 * <p>The base code is fully determined by the owner's own fields, but two distinct owners could in
 * principle hash to the same code. When the computed code collides with an existing owner's
 * {@code customerCode}, {@code -<n>} is appended with the smallest {@code n} of 2 or more that makes
 * it unique, so distinct owners always end up with distinct customer codes.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = CityRegions.identityRegion(owner.getPostcode());
        String normalizedTelephone = OwnerTelephones.toE164OrRaw(owner.getTelephone());
        String hash8 = hash8(normalizedTelephone + owner.getLastName());
        String baseCode = region + "-" + hash8;

        Set<String> existingCodes = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            String code = existing.getCustomerCode();
            if (code != null) {
                existingCodes.add(code);
            }
        }

        String code = baseCode;
        for (int n = 2; existingCodes.contains(code); n++) {
            code = baseCode + "-" + n;
        }
        owner.setCustomerCode(code);
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
                if (sb.length() >= 8) {
                    break;
                }
            }
            return sb.substring(0, 8);
        }
        catch (NoSuchAlgorithmException ex) {
            // SHA-256 is a required algorithm on every JVM, so this cannot happen in practice.
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
