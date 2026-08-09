package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's unified {@code memberId}, formatted {@code '<REGION><FY><HASH8><CHK>'}:
 * <ul>
 *   <li>REGION — the version-2 region code: the plain region derived from the owner's postcode
 *       (preferred), falling back to the city and then to {@code UNKNOWN}, with the fixed {@code 'V2'}
 *       version tag mixed in (e.g. {@code 'NSWV2'}) so the id differs from its version-1 value;</li>
 *   <li>FY — the two-digit FISCAL YEAR of the {@code registrationDate} (the fiscal year starts on
 *       1 July, so a registration date on or after 1 July belongs to the next calendar year);</li>
 *   <li>HASH8 — the first eight UPPER-case hex characters of SHA-256 over
 *       {@code normalizedTelephone + lastName} (the same hash used by the region-and-hash identity);</li>
 *   <li>CHK — a single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * e.g. {@code 'NSWV2271A2B3C4D5'} for an NSW owner registered in fiscal year 27.
 *
 * <p>The core code is a pure function of the region, fiscal year and hash, so two distinct owners can
 * in principle derive the same {@code memberId}. When the computed id collides with an existing
 * owner's {@code memberId}, this step de-duplicates it by appending {@code '-<n>'} with the smallest
 * {@code n} of 2 or more that makes it unique (e.g. 'NSW271A2B3C4D5-2', then 'NSW271A2B3C4D5-3'), so
 * every persisted owner keeps a distinct id.
 *
 * <p>Runs after {@link BuildOwner} (so the registration date is set) and
 * {@link NormalizeOwnerTelephone} (so the telephone is already in canonical E.164 form) and before
 * {@link SaveOwner}, mutating the not-yet-persisted owner in place.
 */
public class AssignMemberId {

    /**
     * Fixed version tag mixed into the region code used inside the identifiers, so every identity-v2
     * identifier differs from its version-1 value. It is mixed into the identifier only, never into
     * the user-facing plain region ('locality').
     */
    private static final String VERSION_TAG = "V2";

    /** Fixed city-to-region table, mirroring the read-time locality derivation. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region to its inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String core = region(owner) + fiscalYear(owner) + hash8(owner);
        String base = core + luhn(core);
        owner.setMemberId(deduplicate(base, owner, ownerRepository));
    }

    /**
     * Returns {@code base} when no existing owner already uses it, otherwise the first of
     * {@code base-2, base-3, ...} that is free — so distinct owners keep distinct member ids.
     */
    private static String deduplicate(String base, Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            String memberId = existing.getMemberId();
            if (memberId != null) {
                taken.add(memberId);
            }
        }
        if (!taken.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The version-2 region code used inside the member id: the plain region (from the postcode when
     * it falls in a known range, else the city, else UNKNOWN) with the fixed {@code 'V2'} tag mixed
     * in, e.g. {@code 'NSWV2'}. Mixing the tag in here is what shifts every member id off its
     * version-1 value. The plain region without the tag still surfaces separately in the read-time
     * {@code locality}.
     */
    private static String region(Owner owner) {
        return plainRegion(owner) + VERSION_TAG;
    }

    /** The plain region derived from the postcode when it falls in a known range, else the city, else UNKNOWN. */
    private static String plainRegion(Owner owner) {
        String byPostcode = regionFromPostcode(owner.getPostcode());
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    private static String regionFromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The two-digit fiscal year of the owner's registration date. The fiscal year starts on 1 July,
     * so a date in July or later belongs to the next calendar year and an earlier date to the current
     * calendar year; the two digits are the last two of that year.
     */
    private static String fiscalYear(Owner owner) {
        LocalDate date = owner.getRegistrationDate();
        int fy = date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
        return String.format("%02d", fy % 100);
    }

    /** First eight UPPER-case hex characters of SHA-256 over (normalized telephone + last name). */
    private static String hash8(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                .digest((telephone + lastName).getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString();
    }

    /** Single Luhn check digit (0-9) over the digits contained in {@code s}; non-digits are skipped. */
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
