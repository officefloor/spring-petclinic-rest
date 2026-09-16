/*
 * Copyright 2002-2017 the original author or authors.
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
 * The fixed region-to-timezone lookup used to derive an owner's timezone.
 *
 * <p>Maps each known region (see {@link CityRegionTable}) to its IANA timezone name:
 * NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane. A
 * region with no known timezone (e.g. {@link CityRegionTable#UNKNOWN}) resolves to
 * {@code null}. This is the single source of truth for the mapping, so callers never
 * repeat the table.
 */
public final class RegionTimezoneTable {

    private static final Map<String, String> TIMEZONE_BY_REGION = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private RegionTimezoneTable() {
    }

    /**
     * Resolve the IANA timezone name for the given region.
     *
     * @param region the canonical region (as returned by {@link CityRegionTable})
     * @return the region's IANA timezone name, or {@code null} when the region has none
     */
    public static String timezoneFor(String region) {
        return TIMEZONE_BY_REGION.get(region);
    }
}
