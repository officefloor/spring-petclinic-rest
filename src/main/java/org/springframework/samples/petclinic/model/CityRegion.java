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
 * The fixed city-to-region lookup used to derive an owner's locality.
 *
 * <p>Maps each known city to its canonical region ({@code Sydney -> NSW},
 * {@code Melbourne -> VIC}, {@code Brisbane -> QLD}); any other city resolves
 * to {@link #UNKNOWN}.
 */
public final class CityRegion {

    /** The region returned for a city that is not in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private CityRegion() {
    }

    /**
     * Return the canonical region for the given city, or {@link #UNKNOWN} when
     * the city is not in the table.
     *
     * @param city the city to look up (may be {@code null})
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    public static String regionFor(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
