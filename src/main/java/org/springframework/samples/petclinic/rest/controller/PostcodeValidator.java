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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.LocalityResolver;
import org.springframework.stereotype.Component;

/**
 * Validates an owner's optional {@code postcode} against the region of their city.
 *
 * <p>A postcode is optional: when absent nothing is checked. When present it must be exactly four
 * digits and, for a city whose region is known (via {@link LocalityResolver}), must fall within
 * that region's inclusive range. A city with no known region accepts any four-digit postcode.
 */
@Component
public class PostcodeValidator {

    private static final Pattern FOUR_DIGITS = Pattern.compile("\\d{4}");

    /** Region -> inclusive four-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * @param postcode the submitted postcode, or {@code null}/absent
     * @param city     the owner's city, used to resolve the applicable region
     * @throws InvalidPostcodeException if the postcode is present but not four digits, or is out of
     *                                  range for the city's region
     */
    public void validate(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_RANGES.get(LocalityResolver.regionFor(city));
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode);
        }
    }
}
