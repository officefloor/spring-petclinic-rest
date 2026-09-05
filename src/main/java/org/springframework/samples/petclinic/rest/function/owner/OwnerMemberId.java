package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

/**
 * Derives an owner's {@code memberId} — the owner's single unified identity, formatted
 * {@code <REGION><FY><HASH8><CHK>} (e.g. {@code NSW261A2B3C4D7}):
 *
 * <ul>
 * <li>{@code REGION} is the region code derived from the postcode: look the postcode up
 * against the inclusive region ranges (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) and,
 * only when the postcode is absent or in no known range, fall back to the fixed
 * city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD), else
 * {@code UNKNOWN}.</li>
 * <li>{@code FY} is the last two digits of the fiscal year of the {@code registrationDate}
 * (fiscal years starting 1 July, identified by the calendar year in which they end).</li>
 * <li>{@code HASH8} is the first 8 upper-case hex characters of SHA-256 over
 * {@code normalizedTelephone + lastName}, where the telephone is normalized to E.164 form
 * (an absent or unparseable telephone contributes an empty string).</li>
 * <li>{@code CHK} is a single Luhn check digit computed over the decimal digits of
 * {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 *
 * <p>The whole owner identity — the {@code memberId} itself, the create audit record and the
 * derived locality — flows from this single value. There is no sequence number;
 * de-duplication of a collided {@code memberId} is handled in {@link AssignMemberId}.
 *
 * <p>Composition and decomposition of the id format live together here: {@link #of} builds an
 * id, {@link #regionOf} reads the region back out of one, and {@link #luhn} is the check over
 * its digits. The owner mapper's {@code locality} delegates to {@link #regionOf} rather than
 * re-deriving the format; its {@code fiscalYear} is a projection of the registration date
 * (see {@link FiscalYear#label}), not read back out of the id.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without
 * tripping the one-public-method-per-function rule.
 */
public final class OwnerMemberId {

    /** City -> canonical region, matching the fixed table used to validate postcodes. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * The fixed version-2 tag mixed into the region code that goes <em>inside</em> the
     * identifiers (see {@link #regionV2}). It is only ever prefixed onto the region segment of
     * the {@code memberId} (and the sibling {@code householdId}/{@code identityKey} tags); the
     * user-facing {@code locality} stays the plain {@link #region} code, so this tag never
     * leaks into {@code locality}, {@code timezone} or the owner segment.
     */
    public static final String VERSION_TAG = "V2";

    private OwnerMemberId() {
    }

    /** The full {@code <REGION><FY><HASH8><CHK>} member id for the given owner fields. */
    public static String of(String postcode, String city, String telephone, String lastName,
            LocalDate registrationDate) {
        String body = regionV2(postcode, city) + fiscalYear(registrationDate) + hash8(telephone, lastName);
        return body + luhn(body);
    }

    /**
     * The version-2 region code used <em>inside</em> the identifiers: the plain {@link #region}
     * code with the fixed {@link #VERSION_TAG 'V2'} version tag mixed in (e.g. {@code NSW ->
     * V2NSW}). Distinct from {@link #region} so every identifier changes and no value produced
     * under version 1 is produced again, while the user-facing {@code locality} keeps the plain
     * region.
     */
    public static String regionV2(String postcode, String city) {
        return VERSION_TAG + region(postcode, city);
    }

    /** The two-digit fiscal-year segment (e.g. {@code 26}) for a registration date. */
    private static String fiscalYear(LocalDate registrationDate) {
        return String.format("%02d", FiscalYear.of(registrationDate) % 100);
    }

    /**
     * The <em>plain</em> REGION segment encoded in an existing member id (e.g. {@code NSW}),
     * i.e. the leading run of letters that precedes the {@code <FY>} digits (see {@link #of}),
     * with the version-2 {@link #VERSION_TAG 'V2'} tag stripped off first so the recovered
     * region never carries it. Returns {@code null} when {@code id} is absent or carries no
     * region segment. The inverse of {@link #of}: callers (such as the mapper's {@code
     * locality}) read the plain region back out of the id here instead of re-parsing the format
     * themselves, keeping the tag inside the identifier only.
     */
    public static String regionOf(String id) {
        if (id == null) {
            return null;
        }
        String rest = id.startsWith(VERSION_TAG) ? id.substring(VERSION_TAG.length()) : id;
        int i = 0;
        while (i < rest.length() && Character.isLetter(rest.charAt(i))) {
            i++;
        }
        return i > 0 ? rest.substring(0, i) : null;
    }

    /**
     * The Luhn check digit (0-9) computed over the decimal digits contained in {@code s}
     * (non-digit characters are ignored). Kept beside the id it checks so the id format and
     * the values derived from it share one home.
     */
    public static int luhn(String s) {
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
     * The region code derived from the postcode (range lookup), falling back to the fixed
     * city-to-region table when the postcode is absent or in no known range, else
     * {@code UNKNOWN}.
     */
    public static String region(String postcode, String city) {
        if (postcode != null) {
            try {
                int value = Integer.parseInt(postcode.trim());
                for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                    int[] range = entry.getValue();
                    if (value >= range[0] && value <= range[1]) {
                        return entry.getKey();
                    }
                }
            }
            catch (NumberFormatException ex) {
                // not a numeric postcode; fall back to the city table
            }
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The region for a city by the fixed city-to-region table
     * ({@code Sydney->NSW, Melbourne->VIC, Brisbane->QLD}), or {@code null} when the city has
     * no known region. Unlike {@link #region}, this does not fall back to {@code UNKNOWN}; it
     * exposes the raw city table so callers such as {@code ValidatePostcode} share this one
     * source of the region reference data.
     */
    public static String regionForCity(String city) {
        return CITY_REGION.get(city);
    }

    /**
     * The inclusive 4-digit postcode range {@code {low, high}} pinned for a region (NSW
     * 2000-2099, VIC 3000-3099, QLD 4000-4099), or {@code null} when the region has no pinned
     * range. The returned array is a copy, so callers cannot mutate the shared table.
     */
    public static int[] postcodeRange(String region) {
        int[] range = REGION_POSTCODES.get(region);
        return range == null ? null : range.clone();
    }

    /**
     * The first 8 upper-case hex characters of SHA-256 over {@code normalizedTelephone +
     * lastName}, where the telephone is normalized to E.164 (an absent or unparseable
     * telephone, or an absent last name, contributes an empty string).
     */
    public static String hash8(String telephone, String lastName) {
        String tel = E164Telephone.normalizeOrNull(telephone);
        String normalizedTelephone = tel == null ? "" : tel;
        String last = lastName == null ? "" : lastName;
        return Sha256.hex(normalizedTelephone + last).substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
