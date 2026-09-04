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
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} where:
 * <ul>
 * <li>REGION is the version-2 region code — the postcode-derived region carrying the fixed 'V2'
 * version tag, e.g. {@code V2NSW} (see {@link OwnerRegion#identityRegion(Owner)}),</li>
 * <li>FY is the two-digit fiscal year (1 July basis) of the {@code registrationDate} (see
 * {@link FiscalYear}),</li>
 * <li>HASH8 is the first eight upper-case hex characters of SHA-256 over the concatenation of the
 * owner's normalized (E.164) telephone and last name — the same value used by the region-and-hash
 * identity, and</li>
 * <li>CHK is a single Luhn check digit computed over the decimal digits of
 * {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * For example {@code V2NSW271A2B3C4D6}. There is no sequence number, so the id is stable for a given
 * region, fiscal year, telephone and last name and does not depend on how many owners already exist.
 * Runs after the customer's registration date has been assigned.
 *
 * <p>Should the computed id collide with an already-assigned {@code memberId} (a hash collision, or
 * the same region, fiscal year and hash on distinct owners), it is de-duplicated by appending
 * {@code -<n>} with the smallest {@code n} of 2 or more that makes it unique among existing owners.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.identityRegion(owner);
        String fy = String.format("%02d", FiscalYear.endingYear(owner.getRegistrationDate()) % 100);
        String body = region + fy + hash8(owner);
        String memberId = body + luhn(body);
        owner.setMemberId(deduplicate(memberId, ownerRepository));
    }

    /**
     * Returns {@code memberId} unchanged when no existing owner already uses it, otherwise
     * {@code memberId-<n>} with the smallest {@code n >= 2} that is not already in use.
     */
    private static String deduplicate(String memberId, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }
        if (!existing.contains(memberId)) {
            return memberId;
        }
        for (int n = 2; ; n++) {
            String candidate = memberId + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }

    /** First eight upper-case hex characters of SHA-256(normalizedTelephone + lastName). */
    private static String hash8(Owner owner) {
        // The stored telephone is already E.164 (see NormalizeOwnerTelephone); re-normalizing is
        // idempotent and falls back to the stored value if it somehow cannot be parsed.
        String normalizedTelephone = E164Telephone.toE164OrNull(owner.getTelephone());
        if (normalizedTelephone == null) {
            normalizedTelephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        }
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((normalizedTelephone + lastName).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /** Luhn check digit (0-9) over the decimal digits contained in {@code value}. */
    private static int luhn(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }
}
