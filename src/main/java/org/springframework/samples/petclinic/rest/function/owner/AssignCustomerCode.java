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
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region code derived from the owner's postcode (the same derivation the locality shares, see
 * {@link OwnerRegion}) and HASH8 is the first 8 upper-case hex characters of SHA-256 over the owner's
 * normalized (E.164) telephone concatenated with the last name.
 *
 * <p>The identity is therefore a stable function of who the owner is, not of how many owners already
 * exist — the old per-city sequence number is gone.
 *
 * <p>Should the computed code collide with an existing owner's {@code customerCode}, it is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it
 * unique, so distinct owners always end up with distinct customer codes.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.of(owner);
        String hash8 = hash8(normalizedTelephone(owner.getTelephone()) + owner.getLastName());
        String base = region + "-" + hash8;
        owner.setCustomerCode(deduplicate(base, existingCustomerCodes(ownerRepository)));
    }

    /** The {@code customerCode}s already assigned to other owners in the data store. */
    private static Set<String> existingCustomerCodes(OwnerRepository ownerRepository) {
        Set<String> codes = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            String code = existing.getCustomerCode();
            if (code != null) {
                codes.add(code);
            }
        }
        return codes;
    }

    /**
     * Returns {@code base} if it is not already taken; otherwise appends {@code -<n>} with the
     * smallest {@code n} of 2 or more that makes it unique.
     */
    private static String deduplicate(String base, Set<String> taken) {
        if (!taken.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The owner's telephone normalized to E.164 (the same way {@link OwnerIdentityKey} does),
     * falling back to the trimmed raw value when it cannot form a valid E.164 number. On the create
     * path the telephone was already normalized upstream, so this is idempotent.
     */
    private static String normalizedTelephone(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = E164Telephone.normalize(raw);
        return normalized != null ? normalized : raw.trim();
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code s}. */
    private static String hash8(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
                if (sb.length() >= 8) {
                    break;
                }
            }
            return sb.substring(0, 8);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
