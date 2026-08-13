package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.Localities;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's {@code memberId}, the single unified owner identity, formatted
 * {@code <REGION><FY><HASH8><CHK>}:
 * <ul>
 * <li>REGION — the region derived from the postcode alone ({@link Localities#ofPostcode}: NSW
 * 2000-2099, VIC 3000-3099, QLD 4000-4099, else {@code UNKNOWN}).</li>
 * <li>FY — the 2-digit fiscal year (last two digits, starting 1 July) of the
 * business-day-adjusted registration date.</li>
 * <li>HASH8 — the first 8 UPPER-case hex characters of the SHA-256 digest over
 * {@code normalizedTelephone + lastName} (the same region-and-hash identity as before).</li>
 * <li>CHK — a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * (e.g. {@code NSW263F9A0C714}). The identity is stable for a given telephone, surname, region and
 * fiscal year. When the computed {@code memberId} collides with an existing owner's, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique, so distinct owners always
 * receive distinct ids.
 * Runs after {@link BuildOwner} maps the request (so the telephone is already the normalized E.164
 * value) and {@link DefaultOwnerRegistrationDate} ensures a registration date, and before
 * {@link SaveOwner} persists it; every downstream region-and-hash value (the locality and the create
 * audit record) flows from this id.
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Localities.ofPostcode(owner.getPostcode());
        String fy = String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100);
        String hash8 = hash8(owner.getTelephone() + owner.getLastName());
        String core = region + fy + hash8;
        String base = core + luhn(core);

        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getMemberId() != null) {
                taken.add(existing.getMemberId());
            }
        }

        String memberId = base;
        for (int n = 2; taken.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }

    /** First 8 UPPER-case hex characters of SHA-256 over the UTF-8 bytes of {@code input}. */
    private static String hash8(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(8);
            for (byte b : digest) {
                hex.append(String.format("%02X", b));
                if (hex.length() >= 8) {
                    break;
                }
            }
            return hex.substring(0, 8);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Single Luhn check digit (0-9) over the digits contained in {@code s}; non-digits are ignored. */
    private static int luhn(String s) {
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
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
