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

import java.util.Map;

/**
 * Fixed table of the inclusive 4-digit postcode range each region permits
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city's region is derived
 * via {@link Locality}; a city whose region is not in the table accepts any
 * 4-digit postcode.
 */
public final class RegionPostcodes {

    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private RegionPostcodes() {
    }

    /**
     * Returns whether {@code postcode} is permitted for the region of {@code city}.
     * A city with no known region accepts any postcode.
     */
    public static boolean isValidForCity(String city, int postcode) {
        int[] range = REGION_RANGES.get(Locality.fromCity(city));
        return range == null || (postcode >= range[0] && postcode <= range[1]);
    }
}
