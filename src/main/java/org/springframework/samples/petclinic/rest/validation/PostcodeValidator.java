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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;

/**
 * Validates the optional postcode submitted when creating an owner. The postcode is validated
 * only <em>when present</em>: a {@code null} postcode is left untouched (an owner created without
 * a postcode stays valid). When present it must be a 4-digit value and, when the owner's city
 * maps to a known region, must fall within that region's fixed inclusive range
 * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}). A city with no known
 * region accepts any 4-digit postcode.
 *
 * <p>An invalid postcode raises an {@link InvalidPostcodeException} so the request is rejected
 * with {@code 400 Bad Request}.
 */
public final class PostcodeValidator {

    /** Fixed city-to-region table ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}). */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private PostcodeValidator() {
    }

    /**
     * Validate the supplied postcode for the given city.
     *
     * @param postcode the raw postcode value (may be {@code null} when absent)
     * @param city the owner's city, used to derive the region whose range applies
     * @throws InvalidPostcodeException if the postcode is present but not a 4-digit value, or is
     *                                  out of range for the city's region
     */
    public static void validate(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("\\d{4}")) {
            throw new InvalidPostcodeException("postcode must be a 4-digit value");
        }
        String region = city == null ? null : CITY_REGION.get(city);
        int[] range = region == null ? null : Regions.REGION_POSTCODES.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(
                    "postcode " + postcode + " is not valid for region " + region);
            }
        }
    }
}
