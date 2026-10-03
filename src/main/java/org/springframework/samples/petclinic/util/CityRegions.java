/*
 * Copyright 2002-2017 the original author or authors.
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

/**
 * Single source of truth for the fixed city-to-region table ({@code Sydney->NSW, Melbourne->VIC,
 * Brisbane->QLD}). Both the owner's derived {@code locality} ({@code OwnerMapper}) and the postcode
 * range rule ({@link PostcodeRules}) consult this one table, so a city added or renamed here is
 * reflected everywhere at once.
 */
public final class CityRegions {

    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private CityRegions() {
    }

    /**
     * Return the canonical region for {@code city}, or {@code null} when the city is not in the
     * fixed table.
     *
     * @param city the owner's city (may be {@code null})
     * @return the region (e.g. {@code "NSW"}) or {@code null} when the city has no known region
     */
    public static String regionOf(String city) {
        if (city == null) {
            return null;
        }
        return CITY_REGION.get(city);
    }
}
