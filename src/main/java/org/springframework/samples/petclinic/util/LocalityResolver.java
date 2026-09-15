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

package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Resolves an owner's locality (canonical region) from its city using a fixed
 * city-to-region table. Cities absent from the table resolve to {@link #UNKNOWN}.
 */
public abstract class LocalityResolver {

    /** Locality returned for any city not present in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Return the canonical region for the given city, or {@link #UNKNOWN} when the
     * city is {@code null} or not in the table.
     */
    public static String localityOf(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

}
