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
 * Fixed lookup from a city to its canonical region code.
 */
public final class CityRegionTable {

    /** Region code returned for any city not present in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGIONS = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private CityRegionTable() {
    }

    /**
     * The canonical region code for the given city, or {@link #UNKNOWN} when the
     * city is not in the table.
     */
    public static String regionOf(String city) {
        return CITY_REGIONS.getOrDefault(city, UNKNOWN);
    }
}
