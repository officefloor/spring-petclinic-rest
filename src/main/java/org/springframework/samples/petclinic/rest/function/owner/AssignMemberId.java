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
 * Assigns the owner's {@code memberId}, formatted {@code '<REGION><FY><HASH8><CHK>'}:
 * <ul>
 *   <li>{@code REGION} — the version-2 identity region derived from the owner (the plain
 *       region with the fixed {@code "V2"} tag mixed in; see
 *       {@link Owner#getIdentityRegion()}),</li>
 *   <li>{@code FY} — the 2-digit fiscal year of the business-day-adjusted
 *       registrationDate (the last two digits of {@link Owner#getFiscalYear()}),</li>
 *   <li>{@code HASH8} — the first 8 upper-case hex characters of
 *       {@code SHA-256(normalizedTelephone + lastName)}, the telephone in the E.164 form
 *       already normalized by {@link ValidateOwnerFields} (the same HASH8 used by the
 *       region-and-hash identity),</li>
 *   <li>{@code CHK} — a single Luhn check digit over the decimal digits of
 *       {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * This unifies what were previously the separate {@code customerCode},
 * {@code membershipNumber} and {@code checkDigit} fields into one identifier.
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner} in the
 * {@code POST /api/owners} pipeline; the id is persisted with the new owner and returned
 * by later reads. It mutates the built {@link Owner} in place (see {@code @Val}
 * semantics).
 *
 * <p>Should the computed id collide with an existing owner's {@code memberId},
 * {@code '-<n>'} is appended with the smallest {@code n} of 2 or more that makes it
 * unique; the de-duplicated id is what gets stored. The new owner is not yet persisted
 * here, so it is never compared against itself.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String hash8 = shaHex8(owner.getTelephone() + owner.getLastName());
        // getFiscalYear() is 'FY<YY>'; the memberId carries just the 2-digit <YY>.
        String fy = owner.getFiscalYear().substring(2);
        String prefix = owner.getIdentityRegion() + fy + hash8;
        String base = prefix + luhn(prefix);

        Set<String> existingIds = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            existingIds.add(existing.getMemberId());
        }

        String memberId = base;
        for (int n = 2; existingIds.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String shaHex8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * The single Luhn check digit (0-9) computed over the decimal digits contained in
     * {@code value}, right to left, doubling every second digit (starting with the
     * rightmost). Non-digit characters (the letters in {@code REGION} and {@code HASH8})
     * are ignored.
     */
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
