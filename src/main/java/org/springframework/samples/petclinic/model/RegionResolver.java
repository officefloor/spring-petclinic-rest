/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.model;

/**
 * Resolves an owner's canonical region: the {@link RegionPostcodes postcode range} is consulted
 * first, falling back to the {@link CityRegions city-to-region table} when the postcode is absent
 * or in no known range, and finally {@link CityRegions#UNKNOWN} when the city is not in the table
 * either. This is the single source of truth for the region an owner belongs to, shared by the
 * customer-code identity and the derived locality.
 */
public final class RegionResolver {

    private RegionResolver() {
    }

    /**
     * Return the region for the given postcode and city.
     *
     * @param postcode the owner's postcode, or {@code null} if none was provided
     * @param city     the owner's city
     * @return the matching region, or {@code UNKNOWN} when neither postcode nor city resolves one
     */
    public static String regionFor(String postcode, String city) {
        String region = RegionPostcodes.regionForPostcode(postcode);
        return region != null ? region : CityRegions.regionOf(city);
    }
}
