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
package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Single source of truth for the fixed region-to-timezone table ({@code NSW->Australia/Sydney,
 * VIC->Australia/Melbourne, QLD->Australia/Brisbane}). The owner's derived read-only
 * {@code timezone} ({@code OwnerMapper}) consults this one table, so a region added or renamed
 * here is reflected everywhere at once.
 */
public final class RegionTimezones {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private RegionTimezones() {
    }

    /**
     * Return the IANA timezone name for {@code region}, or {@code null} when the region is not in
     * the fixed table.
     *
     * @param region the owner's region/locality (may be {@code null})
     * @return the IANA timezone (e.g. {@code "Australia/Sydney"}) or {@code null} when the region
     *         has no mapped timezone
     */
    public static String timezoneOf(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
