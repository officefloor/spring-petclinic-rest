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
 * Assigns the owner's unified {@code memberId} formatted {@code <REGION><FY><HASH8><CHK>}, where
 * REGION is the version-2 region code — the postcode-derived region (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099, otherwise {@code UNKNOWN}) with the fixed {@code V2} tag appended (e.g.
 * {@code NSWV2}) — FY the two-digit fiscal year of the business-day-adjusted
 * registrationDate, HASH8 the first 8 upper-case hex characters of the SHA-256 digest of the
 * normalized (E.164) telephone concatenated with the last name (the same hash used by the
 * region-and-hash identity), and CHK a single Luhn check digit over the digits of
 * {@code <REGION><FY><HASH8>} (e.g. {@code NSWV2261A2B3C4D8}). The identity is deterministic — no
 * sequence numbers — so the same postcode, telephone, last name and fiscal year always produce the
 * same member id. Runs after the owner is built and normalized and its registration date is set.
 *
 * <p>If the computed id collides with an existing owner's {@code memberId}, {@code -<n>} is appended
 * with the smallest {@code n} of 2 or more that makes it unique (e.g. a second collision yields
 * {@code NSW261A2B3C4D8-2}), so distinct owners always receive distinct member ids. The new owner is
 * not yet persisted at this step, so it is not counted among the existing ids.
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        // Version 2: the REGION segment carries the plain region code plus the fixed 'V2' tag
        // (e.g. NSW -> NSWV2), so every member id changes and no version-1 value is reproduced.
        String region = LocalityLookup.postcodeRegion(owner.getPostcode()) + OwnerIdentityVersion.TAG;
        String normalizedTelephone = TelephoneNormalizer.toE164(owner.getTelephone());
        String basis = (normalizedTelephone == null ? "" : normalizedTelephone) + owner.getLastName();
        int fiscalYear = FiscalYear.of(owner.getRegistrationDate());
        String baseId = MemberId.format(region, fiscalYear, hash8(basis));

        Set<String> existingIds = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            String id = other.getMemberId();
            if (id != null) {
                existingIds.add(id);
            }
        }

        String memberId = baseId;
        for (int n = 2; existingIds.contains(memberId); n++) {
            memberId = baseId + "-" + n;
        }
        owner.setMemberId(memberId);
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
