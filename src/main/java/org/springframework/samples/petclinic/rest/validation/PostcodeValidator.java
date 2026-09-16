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

import org.springframework.samples.petclinic.model.CityRegions;
import org.springframework.samples.petclinic.model.RegionPostcodes;

/**
 * Validates an owner's optional postcode against its city. The 4-digit format is enforced by the
 * schema; this checks the semantic rule that a present postcode must fall within the inclusive
 * range fixed for the city's region. A city with no known region accepts any 4-digit postcode.
 */
public final class PostcodeValidator {

    private PostcodeValidator() {
    }

    /**
     * Rejects a postcode that is out of range for its city's region. Postcode is optional, so a
     * {@code null} value is accepted.
     *
     * @param city     the owner's city, used to derive the region
     * @param postcode the submitted 4-digit postcode, or {@code null} if none was provided
     * @throws InvalidPostcodeException if a value is present but out of range for the city's region
     */
    public static void validate(String city, String postcode) {
        if (postcode == null) {
            return;
        }
        String region = CityRegions.regionOf(city);
        if (!RegionPostcodes.isValidForRegion(region, Integer.parseInt(postcode))) {
            throw new InvalidPostcodeException(
                "Postcode '" + postcode + "' is out of range for region '" + region + "'");
        }
    }
}
