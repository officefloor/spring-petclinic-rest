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

package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derivation of an owner's locality (region) from its city. Kept separate from the owner controller
 * and the {@code Owner} model so the rule lives in one place, as a pure function with no web or
 * persistence dependencies. Mirrors {@link AddressNormalizer}, which does the same for addresses.
 *
 * <p>The locality is looked up in a fixed city-to-region table ({@code Sydney -> NSW},
 * {@code Melbourne -> VIC}, {@code Brisbane -> QLD}). The lookup is an exact match; any city not in
 * the table (including {@code null}) derives the locality {@code UNKNOWN}.
 */
public abstract class LocalityResolver {

    /** Locality derived for any city not present in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. Any city not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Derive the canonical region for a city, or {@link #UNKNOWN} when the city is not in the
     * fixed city-to-region table.
     *
     * @param city the owner's city (may be null)
     * @return the canonical region string, or {@code UNKNOWN}
     */
    public static String resolve(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

}
