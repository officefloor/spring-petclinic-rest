package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.LocalityLookup;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode} formatted {@code <REGION>-<HASH8>}, where REGION is the
 * region code derived from the postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099, otherwise
 * {@code UNKNOWN}) and HASH8 is the first 8 upper-case hex characters of the SHA-256 digest of the
 * normalized (E.164) telephone concatenated with the last name (e.g. {@code NSW-1A2B3C4D}). The
 * identity is deterministic — no sequence numbers — so the same telephone and last name always
 * produce the same hash. Runs after the owner is built and normalized.
 *
 * <p>If the computed code collides with an existing owner's {@code customerCode}, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique (e.g. a second collision
 * yields {@code NSW-1A2B3C4D-2}), so distinct owners always receive distinct customer codes. The
 * new owner is not yet persisted at this step, so it is not counted among the existing codes.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = LocalityLookup.postcodeRegion(owner.getPostcode());
        String normalizedTelephone = TelephoneNormalizer.toE164(owner.getTelephone());
        String basis = (normalizedTelephone == null ? "" : normalizedTelephone) + owner.getLastName();
        String baseCode = region + "-" + hash8(basis);

        Set<String> existingCodes = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            String code = other.getCustomerCode();
            if (code != null) {
                existingCodes.add(code);
            }
        }

        String customerCode = baseCode;
        for (int n = 2; existingCodes.contains(customerCode); n++) {
            customerCode = baseCode + "-" + n;
        }
        owner.setCustomerCode(customerCode);
    }

    /** First 8 upper-case hex characters of the SHA-256 digest of the UTF-8 bytes of {@code input}. */
    private static String hash8(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 8).toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
