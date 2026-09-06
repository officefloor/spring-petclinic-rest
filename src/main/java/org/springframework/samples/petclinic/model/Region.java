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
package org.springframework.samples.petclinic.model;

import java.util.Map;
import java.util.Optional;

/**
 * The Australian region an owner's city belongs to. Each constant's name is the canonical region
 * code as stored and returned (e.g. {@code "NSW"}). Regions are resolved from a single fixed
 * city-to-region table ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD});
 * a city that is not in the table has no known region.
 *
 * <p>This is the one place the city-to-region mapping lives, so every rule that keys off an owner's
 * region - such as the derived {@code locality} - resolves it identically.
 */
public enum Region {

    NSW,
    VIC,
    QLD;

    /** Fixed city -> region table; a city absent from this map has no known region. */
    private static final Map<String, Region> BY_CITY = Map.of(
        "Sydney", NSW,
        "Melbourne", VIC,
        "Brisbane", QLD);

    /**
     * Resolves the region for the given city from the fixed city-to-region table.
     *
     * @param city the owner's city, or {@code null}
     * @return the matching region, or empty when the city is {@code null} or has no known region
     */
    public static Optional<Region> forCity(String city) {
        return city == null ? Optional.empty() : Optional.ofNullable(BY_CITY.get(city));
    }
}
