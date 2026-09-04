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
 * Validation of an owner's optional 4-digit postcode against the region derived from its city. Kept
 * separate from the owner controller and the {@code Owner} model so the rule lives in one place, as a
 * pure function with no web or persistence dependencies. Mirrors {@link LocalityResolver}, whose
 * city-to-region derivation this reuses.
 *
 * <p>A postcode must consist of exactly four digits, and when the city maps to a known region (see
 * {@link LocalityResolver}) the numeric value must fall within that region's fixed inclusive range
 * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}). A city with no known region
 * (locality {@code UNKNOWN}) accepts any 4-digit postcode. A {@code null} postcode is not present and
 * is therefore always accepted; the presence check is the caller's responsibility.
 */
public abstract class PostcodeValidator {

    /** A postcode is exactly four digits. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("[0-9]{4}");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Whether the given postcode is valid for the given city. An absent ({@code null}) postcode is
     * valid; a present one must be four digits and, when the city has a known region, must fall within
     * that region's inclusive range. Any city with no known region accepts any 4-digit postcode.
     *
     * @param city     the owner's city (may be null)
     * @param postcode the owner's postcode (may be null when absent)
     * @return {@code true} when the postcode is absent or valid for the city, {@code false} otherwise
     */
    public static boolean isValid(String city, String postcode) {
        if (postcode == null) {
            return true;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            return false;
        }
        int[] range = REGION_POSTCODES.get(LocalityResolver.resolve(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

}
