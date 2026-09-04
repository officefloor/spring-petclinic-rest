/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Derivation of an owner's locality (region) from its postcode and city. Kept separate from the
 * owner controller and the {@code Owner} model so the rule lives in one place, as a pure function
 * with no web or persistence dependencies. Mirrors {@link AddressNormalizer}, which does the same
 * for addresses.
 *
 * <p>The locality is derived by <em>preferring the postcode</em>: a present 4-digit postcode that
 * falls within a known region's fixed inclusive range ({@code NSW 2000-2099}, {@code VIC 3000-3099},
 * {@code QLD 4000-4099}) yields that region directly. Only when the postcode is absent or in no known
 * range does the derivation fall back to a fixed city-to-region table ({@code Sydney -> NSW},
 * {@code Melbourne -> VIC}, {@code Brisbane -> QLD}); that lookup is an exact match. A city not in the
 * table (including {@code null}) with no region-bearing postcode derives the locality {@code UNKNOWN}.
 * Because the pinned cities sit in their region's postcode range this returns the same region for the
 * known cities, while disambiguating cities that merely share a name.
 */
public abstract class LocalityResolver {

    /** Locality derived when neither the postcode nor the city resolves to a known region. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. Any city not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** A postcode is exactly four digits. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("[0-9]{4}");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Derive the canonical region, preferring the postcode over the city. A present 4-digit postcode
     * within a known region's range yields that region; otherwise the city-to-region table is
     * consulted, deriving {@link #UNKNOWN} when the city is absent or unknown.
     *
     * @param city     the owner's city (may be null)
     * @param postcode the owner's postcode (may be null when absent)
     * @return the canonical region string, or {@code UNKNOWN}
     */
    public static String resolve(String city, String postcode) {
        String fromPostcode = regionForPostcode(postcode);
        return fromPostcode != null ? fromPostcode : resolve(city);
    }

    /**
     * Derive the canonical region for a city, or {@link #UNKNOWN} when the city is not in the
     * fixed city-to-region table. This is the city-only fallback; prefer
     * {@link #resolve(String, String)} where a postcode is available.
     *
     * @param city the owner's city (may be null)
     * @return the canonical region string, or {@code UNKNOWN}
     */
    public static String resolve(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The region whose fixed inclusive range contains the given 4-digit postcode, or {@code null}
     * when the postcode is absent, not four digits, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
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

}
