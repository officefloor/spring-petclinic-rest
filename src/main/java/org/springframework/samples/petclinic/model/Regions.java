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
 * The fixed city-to-region lookup shared across the domain. It is the single source of truth for
 * mapping a city to its canonical region (Sydney&rarr;NSW, Melbourne&rarr;VIC, Brisbane&rarr;QLD);
 * cities absent from the table resolve to {@link #UNKNOWN}.
 */
public final class Regions {

    /** The region assigned to any city not present in the lookup table. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGIONS =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Regions() {
    }

    /**
     * @param city the city to resolve
     * @return the canonical region for {@code city}, or {@link #UNKNOWN} when the city is not in the
     * fixed table
     */
    public static String regionOf(String city) {
        return CITY_REGIONS.getOrDefault(city, UNKNOWN);
    }
}
