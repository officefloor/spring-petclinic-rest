package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.FiscalYear;
import org.springframework.samples.petclinic.mapper.RegionCode;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, the single unified identifier formatted
 * {@code '<REGION><FY><HASH8><CHK>'}: REGION is the version-2 identity region (see
 * {@link RegionCode}) — the fixed {@code 'V2'} tag mixed into the canonical region derived from the
 * owner's postcode (falling back to the city table, and {@code "UNKNOWN"} when neither resolves),
 * so e.g. {@code 'V2NSW'} — FY is the two-digit fiscal year of the business-day-adjusted
 * {@code registrationDate}
 * (see {@link FiscalYear}), HASH8 is the first eight upper-case hex characters of SHA-256 over the
 * normalized telephone concatenated with the last name, and CHK is a single Luhn check digit
 * computed over the digits of {@code '<REGION><FY><HASH8>'} (e.g. {@code 'NSW261A2B3C4D7'}). The
 * identity is content-derived and carries no sequence number, so it is stable and independent of
 * creation order.
 *
 * <p>When the computed {@code memberId} collides with an existing owner's memberId, {@code '-<n>'}
 * is appended with the smallest {@code n} of 2 or more that makes it unique, so the stored memberId
 * is always de-duplicated.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} (so the telephone is already E.164) and
 * {@link BuildOwner} (so the entity, hence its city, postcode, last name and registrationDate,
 * exists), and before {@link SaveOwner} (so the new owner is not yet among {@code findAll()}); it
 * mutates the built {@link Owner} in place. Every value built from the memberId — the create audit
 * record and the owner-created event — therefore reflects this version-2 region-and-hash identity.
 * The user-facing {@link Locality} is derived independently and stays the plain, un-tagged region.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = RegionCode.identityRegion(owner.getCity(), owner.getPostcode());
        String fy = String.format("%02d", FiscalYear.endYearOf(owner.getRegistrationDate()) % 100);
        String hash8 = hash8(owner.getTelephone() + owner.getLastName());
        String base = region + fy + hash8;
        String memberId = base + luhn(base);

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }

        String unique = memberId;
        for (int n = 2; existing.contains(unique); n++) {
            unique = memberId + "-" + n;
        }
        owner.setMemberId(unique);
    }

    /** First eight upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 8).toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** The single Luhn check digit (0..9) over the digits contained in {@code value}. */
    private static int luhn(String value) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}
