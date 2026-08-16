package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} where REGION is
 * the version-2 region code derived from the postcode (the plain region with the fixed {@code V2}
 * version tag appended, e.g. {@code NSWV2}; see {@link OwnerLocality#regionCodeV2}), FY is the 2-digit fiscal
 * year of the owner's (business-day-adjusted) registration date (see {@link FiscalYear}), HASH8 is
 * the first eight upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName},
 * and CHK is a single Luhn check digit computed over
 * the digits of {@code <REGION><FY><HASH8>}. The telephone is normalized to E.164 (see
 * {@link TelephoneE164}) exactly as the owner is stored, so the hash is stable regardless of the
 * telephone's input format. This single field replaces the former {@code customerCode} and
 * {@code membershipNumber}.
 *
 * <p>Should that computed memberId collide with an existing owner's {@code memberId}, it is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it
 * unique. Runs before {@link SaveOwner}, so the owner being created is not matched against itself.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerLocality.regionCodeV2(owner.getCity(), owner.getPostcode());
        String fy = FiscalYear.twoDigit(owner.getRegistrationDate());
        String normalizedTelephone = normalizedTelephone(owner.getTelephone());
        String hash8 = sha256Hex8(normalizedTelephone + owner.getLastName());
        String base = region + fy + hash8;
        String memberId = base + luhnCheckDigit(base);
        owner.setMemberId(deduplicate(memberId, ownerRepository));
    }

    /**
     * Returns {@code baseId} if no existing owner already uses it; otherwise appends {@code -<n>}
     * with the smallest {@code n >= 2} that is not taken by any existing owner.
     */
    private static String deduplicate(String baseId, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            String id = other.getMemberId();
            if (id != null) {
                existing.add(id);
            }
        }
        if (!existing.contains(baseId)) {
            return baseId;
        }
        for (int n = 2; ; n++) {
            String candidate = baseId + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }

    /** Single Luhn check digit (0 to 9) over the digits contained in {@code value}; non-digits ignored. */
    private static int luhnCheckDigit(String value) {
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

    /** The owner's telephone in the same canonical E.164 form it is stored with. */
    private static String normalizedTelephone(String telephone) {
        String e164 = TelephoneE164.toE164(telephone);
        return e164 != null ? e164 : (telephone == null ? "" : telephone);
    }

    /** First eight upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String sha256Hex8(String value) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex); // never on a standard JRE
        }
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }
}
