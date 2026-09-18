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

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.CityRegionTable;
import org.springframework.samples.petclinic.model.RegionPostcodeTable;
import org.springframework.stereotype.Component;

/**
 * Validates an owner postcode against the owner's city.
 * <p>
 * A postcode is valid when it is a 4-digit code that is acceptable for the region derived
 * from the city via the fixed {@link CityRegionTable}: within that region's range per the
 * fixed {@link RegionPostcodeTable}, or any 4-digit value when the city has no known region.
 */
@Component
public class PostcodeValidator {

    private static final Pattern FOUR_DIGITS = Pattern.compile("\\d{4}");

    /**
     * @param city     the owner's city, used to resolve the region whose range applies
     * @param postcode a postcode as supplied by the client
     * @return {@code true} when {@code postcode} is a 4-digit code valid for the city
     */
    public boolean isValid(String city, String postcode) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
            return false;
        }
        return RegionPostcodeTable.accepts(CityRegionTable.regionOf(city), Integer.parseInt(postcode));
    }
}
