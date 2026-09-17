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
 * The pinned region-to-timezone table used to derive an owner's timezone from their region (see
 * {@link Locality}). Each region maps to its canonical IANA timezone name; a region not in the
 * table has no known timezone.
 */
public final class Timezone {

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_ZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /**
     * The IANA timezone name for the given region.
     *
     * @param region the canonical region (see {@link Locality}); may be {@code null}.
     * @return the IANA timezone name, or {@code null} when the region is absent or not in the table.
     */
    public static String forRegion(String region) {
        return REGION_ZONE.get(region);
    }
}
