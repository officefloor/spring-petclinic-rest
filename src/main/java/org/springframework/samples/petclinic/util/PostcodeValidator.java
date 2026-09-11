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

import org.springframework.samples.petclinic.rest.advice.InvalidPostcodeException;

/**
 * Validates an owner's optional 4-digit postcode against the fixed range for the region of their
 * city. The region is resolved from the city via {@link LocalityResolver}, and each known region
 * has an inclusive 4-digit range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A postcode is
 * validated only when present: an owner without a postcode is accepted. A city whose region is not
 * in the table ({@code "UNKNOWN"}) accepts any 4-digit postcode. A postcode that falls outside its
 * region's range is rejected with {@link InvalidPostcodeException} (a 400).
 */
public final class PostcodeValidator {

    /** Region -> inclusive 4-digit postcode range {low, high}; regions absent here accept any. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private PostcodeValidator() {
    }

    /**
     * Validates the supplied postcode for the given city. Does nothing when the postcode is
     * {@code null} (postcode is optional). When present, the value is expected to already be a
     * 4-digit string (guaranteed by the request-level bean validation); it is accepted when the
     * city's region is unknown, and otherwise must fall within the region's inclusive range.
     *
     * @param city     the owner's city (may be {@code null})
     * @param postcode the owner's postcode (may be {@code null})
     * @throws InvalidPostcodeException if the postcode is out of range for the city's region
     */
    public static void validate(String city, String postcode) {
        if (postcode == null) {
            return;
        }
        int[] range = REGION_POSTCODES.get(LocalityResolver.resolve(city));
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(
                "Postcode " + postcode + " is out of range for city " + city);
        }
    }
}
