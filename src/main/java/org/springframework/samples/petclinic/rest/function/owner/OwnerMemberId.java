package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerRegion;

/**
 * Single source of truth for an owner's {@code memberId} and everything read back out of it.
 *
 * <p>The id unifies what used to be the separate {@code customerCode} and {@code membershipNumber}
 * (and the standalone {@code checkDigit}) into one value whose four segments run together with no
 * separators, formatted {@code <REGION><FY><HASH8><CHK>}:
 * <ul>
 * <li>{@code REGION} is the owner's version-2 region code — the plain region (derived from the
 * postcode, preferring the postcode and falling back to the city table) with the fixed {@code V2}
 * version tag mixed in (see {@link OwnerRegion#identityRegion}), e.g. {@code V2NSW}. The tag lives
 * only inside the identifier; the user-facing {@code locality} stays the plain region {@code NSW}.</li>
 * <li>{@code FY} is the two-digit fiscal year — the last two digits of the owner's fiscal year, the
 * financial year (starting 1 July) of the business-day-adjusted registration date named by its
 * ending calendar year (see {@link OwnerFiscalYear}), e.g. {@code 27} for an October 2026
 * registration.</li>
 * <li>{@code HASH8} is the first eight upper-case hex characters of the SHA-256 of
 * {@code normalizedTelephone + lastName}, where {@code normalizedTelephone} is the E.164 telephone
 * (see {@link OwnerTelephone#toE164}) — the same region-and-hash identity used before.</li>
 * <li>{@code CHK} is a single Luhn check digit computed over the digits contained in
 * {@code <REGION><FY><HASH8>} (the letters of the region and hash are skipped).</li>
 * </ul>
 *
 * <p>There are no sequence numbers: the id depends only on the owner's own region, fiscal year,
 * telephone and last name, so it can be computed without coordinating through the database. The one
 * exception to the separator-free shape is collision handling: should two owners produce the same id,
 * {@link #deduplicate} appends a {@code -<n>} suffix to keep them distinct (see that method). The
 * user-facing {@code locality} and {@code fiscalYear} are derived independently from the owner's own
 * fields (so the {@code V2} tag never leaks into them); the create audit record reads this id as the
 * owner's primary identifier.
 */
public final class OwnerMemberId {

    private OwnerMemberId() {
    }

    /** The {@code <REGION><FY><HASH8><CHK>} member id for {@code owner}. */
    public static String memberId(Owner owner) {
        String region = OwnerRegion.identityRegion(owner.getCity(), owner.getPostcode());
        String fy = OwnerFiscalYear.yy(owner.getRegistrationDate());
        String base = region + fy + hash8(owner.getTelephone(), owner.getLastName());
        return base + luhn(base);
    }

    /**
     * The {@code HASH8} segment: the first eight upper-case hex characters of the SHA-256 of the
     * normalized (E.164) telephone concatenated with the last name.
     */
    static String hash8(String telephone, String lastName) {
        String normalizedTelephone = OwnerTelephone.toE164(telephone);
        String key = (normalizedTelephone == null ? "" : normalizedTelephone)
                + (lastName == null ? "" : lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * The single Luhn check digit (0-9) over the digits contained in {@code s}. Non-digit characters
     * (the region and hash letters) are skipped; the standard Luhn algorithm doubles every second
     * digit from the right, subtracting 9 when a doubled value exceeds 9, and the check digit is the
     * amount needed to round the running sum up to the next multiple of ten.
     */
    static int luhn(String s) {
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

    /**
     * De-duplicates {@code base} against the {@code existing} member ids: returns {@code base}
     * unchanged when it is not already taken, otherwise appends {@code -<n>} with the smallest
     * {@code n} of 2 or more that makes the id unique.
     */
    public static String deduplicate(String base, Set<String> existing) {
        if (!existing.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }
}
