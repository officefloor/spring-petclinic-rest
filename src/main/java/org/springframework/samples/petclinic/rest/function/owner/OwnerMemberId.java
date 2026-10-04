package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code memberId}: the one unified identity every other
 * derived owner value flows from. It owns both sides of the {@code <REGION><FY><HASH8><CHK>} format
 * so the write-time side ({@link AssignMemberId}, which constructs it) and the read-time side
 * ({@code OwnerMapper.locality}, which recovers the region from it) cannot drift — mirroring how
 * {@link OwnerIdentity}, {@link OwnerTelephone} and {@link OwnerLocality} centralise their rules.
 *
 * <p>The format is {@code V2<REGION><FY><HASH8><CHK>} (e.g. {@code V2NSW271A2B3C4D5}) where:
 * <ul>
 *   <li>{@code V2} is the {@link #VERSION_TAG version-2 tag} folded into the region code so the
 *       identifier is rederived and shares no value with a version-1 member id;</li>
 *   <li>{@code REGION} is the owner's region ({@link #regionFor}): the postcode's region, falling
 *       back to the city-to-region table, and {@link #UNKNOWN_REGION} when neither resolves;</li>
 *   <li>{@code FY} is the two-digit fiscal year (starting 1 July) that contains the
 *       business-day-adjusted {@code registrationDate}, derived via {@link FiscalYear#yy} — the
 *       single source of truth shared with the read-time {@code fiscalYear} label;</li>
 *   <li>{@code HASH8} is the first 8 upper-case hex characters of SHA-256 over the owner's
 *       canonical telephone ({@link OwnerTelephone#toE164OrRaw(String)}) concatenated with its
 *       last name — the same region-and-hash identity {@code HASH8};</li>
 *   <li>{@code CHK} is a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 *
 * <p>{@code REGION} is a fixed set of letters ({@code NSW}/{@code VIC}/{@code QLD}/{@code UNKNOWN})
 * and the segments that follow it start with the two {@code FY} digits, so {@link #regionOf} reads
 * the region back (after stripping the leading {@link #VERSION_TAG}) as the leading run of letters.
 */
public final class OwnerMemberId {

    /** Region sentinel used when neither the postcode nor the city resolves to a known region. */
    public static final String UNKNOWN_REGION = "UNKNOWN";

    /**
     * Fixed version-2 tag mixed into the region code carried <em>inside</em> the member id, so every
     * version-2 member id differs from (and shares no value with) any version-1 one. The tag only
     * appears inside the identifier: the read-time {@code locality}, {@code timezone} and owner
     * segment recover the plain region ({@link #regionOf}) and never show the tag.
     */
    public static final String VERSION_TAG = "V2";

    private static final char SEPARATOR = '-';

    private OwnerMemberId() {
    }

    /**
     * The {@code V2<REGION><FY><HASH8><CHK>} {@code memberId} for the owner's current fields. The
     * leading {@link #VERSION_TAG} is the version-2 tag folded into the region code, so the whole
     * identifier (including its Luhn check digit) is rederived and no value collides with a
     * version-1 member id.
     */
    public static String of(Owner owner) {
        String base = VERSION_TAG + regionFor(owner) + FiscalYear.yy(owner.getRegistrationDate())
                + hash8(OwnerTelephone.toE164OrRaw(owner.getTelephone()) + owner.getLastName());
        return base + luhn(base);
    }

    /**
     * The owner's region for identity and display: the postcode/city region (see
     * {@link OwnerLocality#regionFor}), or {@link #UNKNOWN_REGION} when neither resolves.
     */
    public static String regionFor(Owner owner) {
        String region = OwnerLocality.regionFor(owner.getPostcode(), owner.getCity());
        return region != null ? region : UNKNOWN_REGION;
    }

    /**
     * A de-duplicated variant of {@code base}, formed by appending {@code -<n>} (e.g.
     * {@code NSW271A2B3C4D5} with {@code n == 2} becomes {@code NSW271A2B3C4D5-2}). Used when the
     * derived {@code memberId} collides with an existing owner's: the caller picks the smallest
     * {@code n} of 2 or more that is free. Keeping the suffix spelling here, next to {@link #of} and
     * {@link #regionOf}, keeps this class the single authority for the {@code memberId} format — the
     * {@code REGION} prefix is unchanged, so {@link #regionOf} still reads the region from the
     * leading letters.
     */
    public static String withSuffix(String base, int n) {
        return base + SEPARATOR + n;
    }

    /**
     * The plain {@code REGION} carried by a {@code memberId} (e.g. {@code NSW}), with the
     * version-2 {@link #VERSION_TAG} stripped so callers see the user-facing region and never the
     * tag. Reads the leading {@link #VERSION_TAG} (when present, as every version-2 member id
     * carries it) then the leading run of letters before the first {@code FY} digit. Returns
     * {@code null} when {@code memberId} is null or carries no region. A legacy version-1 member id
     * (no {@link #VERSION_TAG} prefix) still reads back its region unchanged.
     */
    public static String regionOf(String memberId) {
        if (memberId == null) {
            return null;
        }
        String rest = memberId.startsWith(VERSION_TAG) ? memberId.substring(VERSION_TAG.length())
                : memberId;
        int i = 0;
        while (i < rest.length() && Character.isLetter(rest.charAt(i))) {
            i++;
        }
        return i > 0 ? rest.substring(0, i) : null;
    }

    /** The first 8 upper-case hex characters (4 bytes) of SHA-256 over {@code value}. */
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
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** A single Luhn check digit (0-9) over the digits contained in {@code value}. */
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
