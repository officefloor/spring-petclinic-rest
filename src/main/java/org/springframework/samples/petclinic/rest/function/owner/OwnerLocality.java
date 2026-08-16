package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's canonical region ({@code locality}), preferring the postcode.
 *
 * <p>The postcode range is consulted first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); a
 * 4-digit postcode landing in a known range resolves the region directly. Only when the postcode
 * is absent or falls in no known range does the fixed city-to-region table apply (Sydney-&gt;NSW,
 * Melbourne-&gt;VIC, Brisbane-&gt;QLD). Anything unresolved by either is {@code "UNKNOWN"}.
 *
 * <p>Known cities keep the same region as before, but cities that share a name are disambiguated
 * by their postcode.
 */
public final class OwnerLocality {

    /**
     * Fixed identity version tag mixed into the region code used INSIDE the identifiers
     * (see {@link #regionCodeV2(String, String)}), and — via that region code and this same tag —
     * into the {@code householdId}, {@code identityKey} and {@code memberId}. It never appears in
     * the user-facing {@code locality}, {@code timezone} or the owner segment's derived region,
     * which stay the plain region code.
     */
    public static final String VERSION_TAG = "V2";

    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** City -> canonical region (matching {@link RequirePostcode}'s table). */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private OwnerLocality() {
    }

    /**
     * The user-facing locality of an already-stored owner: the PLAIN region code derived straight
     * from its postcode/city (see {@link #of(String, String)}), never carrying the {@link #VERSION_TAG}.
     * It is deliberately NOT read back out of the {@code memberId}, whose REGION segment now embeds
     * the version-2 tag (see {@link #regionCodeV2(String, String)} and {@link AssignMemberId}); the
     * {@code locality}, {@code timezone} and owner segment must stay the plain region.
     */
    public static String forOwner(Owner owner) {
        return of(owner.getCity(), owner.getPostcode());
    }

    /**
     * The version-2 region code used INSIDE the identifiers: the plain region (see
     * {@link #of(String, String)}) with the fixed {@link #VERSION_TAG} appended (e.g. {@code "NSWV2"}
     * for NSW). Mixing the tag in here guarantees every version-2 {@code memberId} differs from —
     * and is never equal to — any value produced under version 1. This is the identifier form only;
     * the user-facing {@link #forOwner(Owner) locality} keeps the plain region.
     */
    public static String regionCodeV2(String city, String postcode) {
        return of(city, postcode) + VERSION_TAG;
    }

    /**
     * The IANA timezone for an already-stored owner, derived from its {@link #forOwner(Owner)}
     * locality via the fixed region-to-timezone table. {@code null} when the locality resolves
     * to no known region (e.g. {@code "UNKNOWN"}).
     */
    public static String timezoneForOwner(Owner owner) {
        return REGION_TIMEZONE.get(forOwner(owner));
    }

    static String of(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    private static String regionForPostcode(String postcode) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
