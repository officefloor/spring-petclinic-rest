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
 * Derives an owner's marketing segment, formatted {@code <TIER>_<AREA>}: the TIER reflects the
 * owner's membership level and the AREA reflects whether their locality is a known region.
 */
public final class OwnerSegment {

    /** Lowest membership level that qualifies for the PREMIUM tier. */
    private static final int PREMIUM_MIN_LEVEL = 3;

    private OwnerSegment() {
    }

    /**
     * Return the segment for the given membership level and locality: TIER is {@code PREMIUM} when
     * {@code membershipLevel} is 3 or more, otherwise {@code STANDARD}; AREA is {@code METRO} when
     * {@code locality} is a known region (see {@link CityRegions#isKnownRegion(String)}), otherwise
     * {@code REGIONAL}.
     */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= PREMIUM_MIN_LEVEL ? "PREMIUM" : "STANDARD";
        String area = CityRegions.isKnownRegion(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
