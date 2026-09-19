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
 * Derives an owner's {@code timezone} (an IANA name) from their canonical region (locality)
 * using a fixed region-to-timezone table.
 */
public final class TimezoneResolver {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private TimezoneResolver() {
    }

    /**
     * Resolve the IANA timezone name for the given region.
     *
     * @param region the owner's canonical region (locality), may be {@code null}
     * @return the IANA timezone name from the fixed table, or {@code null} when the region
     * maps to no known timezone
     */
    public static String forRegion(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
