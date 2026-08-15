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
     * The locality of an already-stored owner: the REGION segment of its {@code customerCode}
     * identity ({@code <REGION>-<HASH8>}, see {@link AssignCustomerCode}). Falls back to deriving
     * the region straight from the postcode/city for an owner whose {@code customerCode} has not
     * been assigned or is not in the region-and-hash form.
     */
    public static String forOwner(Owner owner) {
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            if (dash > 0) {
                return code.substring(0, dash);
            }
        }
        return of(owner.getCity(), owner.getPostcode());
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
