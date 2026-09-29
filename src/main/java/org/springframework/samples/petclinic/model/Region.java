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
 * Single source of truth for the fixed city-to-region mapping (Sydney->NSW, Melbourne->VIC,
 * Brisbane->QLD). Consumed by presentation (an owner's {@code locality}) and by validation (an
 * owner's postcode range), so both layers agree on which region a city belongs to.
 */
public final class Region {

    /** Locality reported for a city that has no known region. */
    public static final String UNKNOWN = "UNKNOWN";

    /** Fixed city-to-region lookup. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Region() {
    }

    /**
     * The canonical region for {@code city}, or {@code null} when the city has no known region.
     */
    public static String forCity(String city) {
        return CITY_REGION.get(city);
    }

    /**
     * The canonical region for {@code city}, or {@link #UNKNOWN} when the city has no known region.
     */
    public static String localityForCity(String city) {
        String region = forCity(city);
        return region == null ? UNKNOWN : region;
    }
}
