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

import org.springframework.samples.petclinic.model.CityRegion;
import org.springframework.samples.petclinic.model.PostcodeRegion;

/**
 * Validates an owner's optional postcode against the fixed postcode range of its
 * city's region.
 *
 * <p>The ranges live in {@link PostcodeRegion} and are keyed by the region derived
 * from the city via {@link CityRegion}: {@code NSW 2000-2099}, {@code VIC 3000-3099}
 * and {@code QLD 4000-4099} (all inclusive). A city whose region has no known range
 * (including {@link CityRegion#UNKNOWN}) accepts any postcode. Format (four
 * digits) is enforced separately by Bean Validation on the request DTO, so this
 * class only checks membership of the region's range.
 */
public final class PostcodeValidator {

    private PostcodeValidator() {
    }

    /**
     * Returns whether the given postcode is acceptable for the given city.
     *
     * <p>An absent postcode is always acceptable (the field is optional), and a
     * city whose region has no known range accepts any postcode. Otherwise the
     * postcode must fall within its region's inclusive range.
     *
     * @param city the owner's city
     * @param postcode the submitted postcode, may be {@code null} or blank
     * @return {@code true} when the postcode is acceptable for the city
     */
    public static boolean isValidForCity(String city, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return true;
        }
        String region = CityRegion.regionFor(city);
        if (!PostcodeRegion.hasRange(region)) {
            return true;
        }
        // Ranges are disjoint, so the postcode is valid for the city's region
        // exactly when its own region matches.
        return region.equals(PostcodeRegion.regionFor(postcode));
    }
}
