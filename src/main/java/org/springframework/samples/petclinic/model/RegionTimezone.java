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
 * Fixed table mapping a canonical region to its IANA timezone name
 * (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane).
 * A region's locality is derived via {@link Locality}; a region not in the table has
 * no known timezone.
 */
public final class RegionTimezone {

    private static final Map<String, String> REGION_TO_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private RegionTimezone() {
    }

    /**
     * Returns the IANA timezone name for the given region, or {@code null} when the
     * region is {@code null} or not present in the table.
     */
    public static String forRegion(String region) {
        return region == null ? null : REGION_TO_TIMEZONE.get(region);
    }
}
