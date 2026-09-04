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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Resolves an owner's city to its canonical region using a single fixed table.
 *
 * <p>Keeping this in one place means every rule that is keyed by an owner's region reads
 * the same city-to-region mapping, so they can never drift apart: the region a city
 * derives here is the region every region-keyed rule sees for that city.
 *
 * <p>A city not listed in the table has no known region and resolves to
 * {@link #UNKNOWN_REGION}.
 */
@Component
public class CityRegionResolver {

    /** The region returned for a city that is not in the fixed {@link #CITY_REGION} table. */
    public static final String UNKNOWN_REGION = "UNKNOWN";

    /** Fixed city-to-region table; a city not listed here has no known region. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Resolve a city to its canonical region.
     *
     * @param city the owner's city, as stored
     * @return the region the city belongs to, or {@link #UNKNOWN_REGION} when the city is
     *         not in the fixed table
     */
    public String regionFor(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN_REGION);
    }
}
