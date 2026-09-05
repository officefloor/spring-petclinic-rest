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
package org.springframework.samples.petclinic.service;

import java.util.Map;

/**
 * Derives an owner's locality (canonical region) from its city using a fixed
 * city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD), returning
 * "UNKNOWN" for any city not in the table.
 */
public final class LocalityResolver {

    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Postcode hundreds-prefix (postcode / 100) -&gt; region, one per known range. */
    private static final Map<Integer, String> POSTCODE_REGION =
        Map.of(20, "NSW", 30, "VIC", 40, "QLD");

    private LocalityResolver() {
    }

    public static String locality(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Prefer the postcode's region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099),
     * falling back to the city-to-region table when the postcode is absent or in no
     * known range.
     */
    public static String locality(String city, String postcode) {
        if (postcode != null) {
            String region = POSTCODE_REGION.get(Integer.parseInt(postcode) / 100);
            if (region != null) {
                return region;
            }
        }
        return locality(city);
    }
}
