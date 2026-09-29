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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;
import java.util.Set;

/**
 * The single, shared vocabulary of region codes and their fixed inclusive 4-digit postcode ranges
 * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}). Both the code that
 * <em>derives</em> an owner's region from a postcode (member-id generation) and the code that
 * <em>reads</em> the region back out of a derived member id (the presentation mapper) source the set
 * from here, so adding a region is a one-line change kept automatically in sync between generator and
 * parser.
 */
public final class Regions {

    /** Region-to-inclusive 4-digit postcode range {@code {low, high}}. */
    public static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Region code used when a postcode maps to no known region (or is absent). */
    public static final String UNKNOWN = "UNKNOWN";

    private Regions() {
    }

    /** The known region codes (excludes {@link #UNKNOWN}). */
    public static Set<String> known() {
        return REGION_POSTCODES.keySet();
    }

    /**
     * The region whose inclusive postcode range contains {@code postcode}, or {@link #UNKNOWN} when
     * the postcode is absent, not a 4-digit value, or in no known range.
     */
    public static String forPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return UNKNOWN;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return UNKNOWN;
    }
}
