package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.common.FiscalYear;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Assigns the owner's unified {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} where
 * REGION is the version-2 {@link Localities#identityRegion identity region} (the plain region with
 * the fixed {@code V2} tag mixed in, e.g. {@code NSWV2}), FY is the two-digit fiscal year (starting
 * 1 July) of the business-day-adjusted {@code registrationDate}, HASH8 is the first eight upper-case
 * hex characters of SHA-256 over the {@code V2} tag, the normalized telephone and the last name, and
 * CHK is a single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>} (e.g.
 * {@code NSWV2261A2B3C4D7}). Mixing the {@code V2} tag into both the region and the hash guarantees no
 * version-1 memberId is ever reproduced. Runs after telephone normalization and
 * the registration date default, so the hash is taken over the stored, normalized (E.164) telephone
 * and the fiscal year is known.
 *
 * <p>If the computed memberId collides with an existing owner's {@code memberId}, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique, so distinct owners always
 * receive distinct member ids. Runs before the owner is saved, so it is matched only against owners
 * already present.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Localities.identityRegion(owner.getPostcode(), owner.getCity());
        String fy = String.format("%02d", FiscalYear.endingYear(owner.getRegistrationDate()) % 100);
        String hash8 = shaHex(
                Localities.IDENTITY_VERSION_TAG + owner.getTelephone() + owner.getLastName(), 8);
        String core = region + fy + hash8;
        String base = core + luhn(core);

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            existing.add(other.getMemberId());
        }

        String memberId = base;
        for (int n = 2; existing.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }

    /**
     * The single Luhn check digit (0-9) over the digits contained in {@code value}. Non-digit
     * characters (the region letters and hex letters) are ignored; the digits are processed
     * right-to-left, doubling every second digit starting with the rightmost and subtracting 9 from
     * any doubled value above 9; the check digit is {@code (10 - (sum % 10)) % 10}.
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

    /** First {@code length} upper-case hex characters of SHA-256({@code value}). */
    private static String shaHex(String value, int length) {
        return sha256hex(value).substring(0, length).toUpperCase();
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
