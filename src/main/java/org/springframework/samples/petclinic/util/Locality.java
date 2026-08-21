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
 * Derives an owner's locality (region) from their city using a fixed
 * city-to-region table. Cities not in the table resolve to {@code "UNKNOWN"}.
 */
public final class Locality {

    /** City -> canonical region; anything not listed resolves to "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Value returned for any city that is not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /**
     * @param city the owner's city (may be {@code null}).
     * @return the canonical region for {@code city}, or {@code "UNKNOWN"} when the
     *         city is not in the fixed table.
     */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
