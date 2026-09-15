/*
 * Copyright 2016 the original author or authors.
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
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.stereotype.Component;

/**
 * Validates an optional owner postcode against the region of the owner's city. When present a
 * postcode must be exactly four digits and, for a city whose region is known, fall within that
 * region's inclusive range. A city with no known region accepts any 4-digit postcode. An absent
 * (null) postcode is left unvalidated, keeping the create contract backward-compatible.
 */
@Component
public class PostcodeValidator {

    /** A postcode must be exactly four digits. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * @param postcode the raw submitted postcode (may be {@code null})
     * @param city the owner's city, used to resolve the region that fixes the valid range
     * @throws InvalidPostcodeException if the postcode is present but not four digits, or is out of
     *         range for the region of the given city
     */
    public void validate(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new InvalidPostcodeException("Postcode must be four digits, but was: " + postcode);
        }
        int[] range = REGION_RANGE.get(LocalityResolver.localityOf(city));
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
