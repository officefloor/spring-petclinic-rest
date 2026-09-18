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
 * The fixed region-to-timezone lookup used to derive an owner's timezone.
 *
 * <p>Maps each known region to its IANA timezone name ({@code NSW ->
 * Australia/Sydney}, {@code VIC -> Australia/Melbourne}, {@code QLD ->
 * Australia/Brisbane}); any other region resolves to {@code null}.
 */
public final class RegionTimezone {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private RegionTimezone() {
    }

    /**
     * Return the IANA timezone name for the given region, or {@code null} when the
     * region is unknown or {@code null}.
     *
     * @param region the canonical region to look up (may be {@code null})
     * @return the IANA timezone name, or {@code null}
     */
    public static String zoneFor(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
