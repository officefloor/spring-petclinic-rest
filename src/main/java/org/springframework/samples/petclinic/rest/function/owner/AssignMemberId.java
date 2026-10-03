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
 * Assigns the owner's unified {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>}:
 * <ul>
 * <li>REGION is the region code derived from the postcode (NSW/VIC/QLD, or {@code UNKNOWN}
 * when the postcode is absent or in no known range);</li>
 * <li>FY is the 2-digit fiscal year (starting 1 July) of the registration date;</li>
 * <li>HASH8 is the first 8 upper-case hex characters of the SHA-256 digest over the
 * normalized telephone concatenated with the last name (the same hash used by the
 * region-and-hash identity); and</li>
 * <li>CHK is a single Luhn check digit computed over the digits of
 * {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * For example {@code NSW26A1B2C3D45}. Runs after the telephone has been normalized to
 * E.164, so the hash is stable for a given owner.
 *
 * <p>When the computed memberId collides with an existing owner's {@code memberId}, it is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that
 * makes it unique (e.g. {@code NSW26A1B2C3D45-2}), so distinct owners always receive
 * distinct member ids.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Owner.identityRegion(owner.getPostcode());
        String fy = owner.getRegistrationDate() == null ? "00"
            : String.format("%02d", Owner.fiscalYearOf(owner.getRegistrationDate()) % 100);
        String hash8 = sha256UpperHex8(owner.getTelephone() + owner.getLastName());
        String core = region + fy + hash8;
        String base = core + luhn(core);

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }

        String memberId = base;
        for (int n = 2; existing.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }

    /**
     * The first 8 upper-case hex characters of the SHA-256 digest of the UTF-8 bytes of
     * {@code value}.
     */
    private static String sha256UpperHex8(String value) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString();
    }

    /**
     * A single Luhn check digit (0-9) over the digits contained in {@code value}, scanning
     * right to left and doubling the rightmost digit first; non-digit characters are ignored.
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
