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

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.CityRegionTable;
import org.springframework.samples.petclinic.model.RegionPostcodeTable;
import org.springframework.stereotype.Component;

/**
 * Validates an owner's postcode. A postcode is optional, but when supplied it must be
 * exactly four digits and fall within the range valid for the region derived from the
 * owner's city (see {@link CityRegionTable} and {@link RegionPostcodeTable}). A city
 * whose region has no known range accepts any 4-digit postcode.
 */
@Component
public class PostcodeValidator {

    private static final Pattern FOUR_DIGITS = Pattern.compile("\\d{4}");

    /**
     * @param postcode a supplied (non-{@code null}) postcode value
     * @param city     the owner's city, used to derive the applicable region
     * @return {@code true} if the postcode is four digits and valid for the city's region
     */
    public boolean isValid(String postcode, String city) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
            return false;
        }
        return RegionPostcodeTable.accepts(CityRegionTable.regionFor(city), Integer.parseInt(postcode));
    }
}
