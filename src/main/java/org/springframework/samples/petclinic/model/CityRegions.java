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
import java.util.Set;

/**
 * Fixed lookup of a city to its canonical region (locality).
 */
public final class CityRegions {

    /** Region returned for any city that is not in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_TO_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** The canonical regions (NSW, VIC, QLD); anything else is {@link #UNKNOWN}. */
    private static final Set<String> KNOWN_REGIONS = Set.copyOf(CITY_TO_REGION.values());

    private CityRegions() {
    }

    /**
     * Return the canonical region for the given city, or {@link #UNKNOWN} when
     * the city is not in the table.
     */
    public static String regionOf(String city) {
        return CITY_TO_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Return whether the given region is a known canonical region (NSW, VIC or QLD)
     * rather than {@link #UNKNOWN} or an unrecognised value.
     */
    public static boolean isKnownRegion(String region) {
        return KNOWN_REGIONS.contains(region);
    }
}
