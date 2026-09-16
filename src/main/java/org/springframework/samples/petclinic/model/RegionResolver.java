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

/**
 * Resolves an owner's canonical region from its location. Prefers the postcode,
 * resolving the region whose range contains it (see {@link RegionPostcodeTable}),
 * and only falls back to the city via the fixed {@link CityRegionTable} when the
 * postcode is absent or in no known range.
 *
 * <p>Single source of truth for the mapping so the customer code's region segment
 * and the owner's derived locality are always computed the same way.
 */
public final class RegionResolver {

    private RegionResolver() {
    }

    /**
     * Resolve the canonical region for the given postcode and city.
     *
     * @param postcode the owner's postcode, which may be {@code null} or non-numeric
     * @param city     the owner's city, used when the postcode resolves to no region
     * @return the region owning the postcode's range, or the city's region otherwise
     */
    public static String regionFor(String postcode, String city) {
        return RegionPostcodeTable.regionFor(postcode)
            .orElseGet(() -> CityRegionTable.regionFor(city));
    }
}
