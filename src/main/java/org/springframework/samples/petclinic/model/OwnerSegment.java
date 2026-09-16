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
 * The marketing segment an owner falls into, formatted {@code '<TIER>_<AREA>'}. Owns the
 * tier and area boundaries so they live in one place rather than being scattered across
 * callers: the tier is {@code PREMIUM} at membership level 3 or more and {@code STANDARD}
 * below it, and the area is {@code METRO} for a known region (see
 * {@link CityRegionTable#isKnown(String)}) and {@code REGIONAL} otherwise.
 */
public final class OwnerSegment {

    /** The lowest membership level that earns the {@code PREMIUM} tier. */
    private static final int PREMIUM_LEVEL = 3;

    private OwnerSegment() {
    }

    /**
     * Format the {@code '<TIER>_<AREA>'} segment for an owner.
     *
     * @param membershipLevel the owner's membership level (1 to 4)
     * @param locality        the owner's canonical region (as returned by
     *                        {@link CityRegionTable#regionFor(String)})
     * @return one of {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO}
     *         or {@code STANDARD_REGIONAL}
     */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= PREMIUM_LEVEL ? "PREMIUM" : "STANDARD";
        String area = CityRegionTable.isKnown(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
