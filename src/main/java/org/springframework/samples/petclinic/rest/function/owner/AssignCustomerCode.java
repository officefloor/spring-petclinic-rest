package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region code derived from the owner's postcode (its inclusive range: {@code NSW 2000-2099},
 * {@code VIC 3000-3099}, {@code QLD 4000-4099}, else {@code UNKNOWN}) and HASH8 is the first 8
 * upper-case hex characters of {@code SHA-256} over the owner's normalized telephone concatenated
 * with its last name (e.g. {@code NSW-1A2B3C4D}).
 *
 * <p>The identity is derived deterministically from the owner's own fields — no sequence numbers —
 * so it does not depend on other owners or on creation order. The telephone has already been
 * normalized to E.164 form by {@link ValidateOwnerFields} before this step runs.
 *
 * <p>Should the deterministic code collide with an existing owner's {@code customerCode}, it is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it
 * unique (e.g. {@code NSW-1A2B3C4D}, then {@code NSW-1A2B3C4D-2}, {@code NSW-1A2B3C4D-3}, …).
 */
public class AssignCustomerCode {

    /** Region -&gt; inclusive 4-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private static final String UNKNOWN = "UNKNOWN";

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = regionOf(owner.getPostcode());
        String hash8 = hash8(owner.getTelephone() + owner.getLastName());
        String base = region + "-" + hash8;

        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCustomerCode() != null) {
                taken.add(existing.getCustomerCode());
            }
        }

        String code = base;
        for (int n = 2; taken.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }

    /** The region whose inclusive range contains {@code postcode}, or {@code UNKNOWN} when the
     *  postcode is absent, not four digits, or in no known range. */
    private static String regionOf(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return UNKNOWN;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return UNKNOWN;
    }

    /** The first 8 upper-case hex characters (4 bytes) of {@code SHA-256(value)}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (Exception ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
