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
import java.util.Set;

/**
 * Derives an owner's {@code locality} (canonical region), preferring the postcode range
 * (via {@link PostcodePolicy}) and falling back to a fixed city-to-region table when the
 * postcode is absent or in no known range.
 */
public final class LocalityResolver {

    private static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private static final Set<String> KNOWN_REGIONS = Set.of("NSW", "VIC", "QLD");

    private LocalityResolver() {
    }

    /**
     * Resolve the canonical region for an owner, preferring the postcode.
     *
     * @param city the owner's city, may be {@code null}
     * @param postcode the owner's postcode, may be {@code null}
     * @return the region derived from the postcode range when it is known, otherwise the
     * region from the city table, or {@code "UNKNOWN"} when neither is known
     */
    public static String resolve(String city, String postcode) {
        return PostcodePolicy.regionForPostcode(postcode).orElseGet(() -> regionForCity(city));
    }

    /**
     * Resolve an owner's locality from its {@code customerCode}, whose {@code REGION} prefix is the
     * canonical region. Falls back to {@link #resolve(String, String)} for owners that predate the
     * customer code (and so have none), keeping their locality unchanged.
     *
     * @param customerCode the owner's customer code, may be {@code null}
     * @param city the owner's city, used only for the fallback, may be {@code null}
     * @param postcode the owner's postcode, used only for the fallback, may be {@code null}
     * @return the region carried by the customer code, or the city/postcode-derived region when the
     * customer code is absent
     */
    public static String fromCustomerCode(String customerCode, String city, String postcode) {
        String region = CustomerCodeGenerator.region(customerCode);
        return region != null ? region : resolve(city, postcode);
    }

    /**
     * Resolve the canonical region for the given city using the fixed city-to-region table.
     *
     * @param city the owner's city, may be {@code null}
     * @return the region string from the fixed table, or {@code "UNKNOWN"} when the
     * city is not in the table
     */
    public static String regionForCity(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Report whether the given region is one of the known canonical regions (NSW, VIC or QLD),
     * as opposed to {@code "UNKNOWN"} or any unrecognised value.
     *
     * @param region the canonical region (locality), may be {@code null}
     * @return {@code true} when the region is a known region, {@code false} otherwise
     */
    public static boolean isKnownRegion(String region) {
        return KNOWN_REGIONS.contains(region);
    }
}
