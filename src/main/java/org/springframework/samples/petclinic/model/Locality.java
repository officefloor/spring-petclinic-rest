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

/**
 * Derives an owner's locality (canonical region) from its city and postcode.
 *
 * <p>The postcode takes precedence: when it falls in a known {@link PostcodeRegion}
 * range that region is used, which disambiguates cities that share a name. Only
 * when the postcode is absent or in no known range does the derivation fall back
 * to the {@link CityRegion} city-to-region table (yielding {@link CityRegion#UNKNOWN}
 * for an unlisted city).
 */
public final class Locality {

    private Locality() {
    }

    /**
     * Return the canonical region for the given city and postcode, preferring the
     * postcode range and falling back to the city table.
     *
     * @param city the owner's city (may be {@code null})
     * @param postcode the owner's postcode (may be {@code null} or blank)
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    public static String of(String city, String postcode) {
        String region = PostcodeRegion.regionFor(postcode);
        return region != null ? region : CityRegion.regionFor(city);
    }
}
