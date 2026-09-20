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
 * Derives an owner's segment, formatted {@code "<TIER>_<AREA>"}. TIER is {@link #PREMIUM}
 * when the membership level is {@value #PREMIUM_MIN_LEVEL} or more, otherwise
 * {@link #STANDARD}. AREA is {@link #METRO} when the locality is a known region (NSW, VIC
 * or QLD), otherwise {@link #REGIONAL}.
 */
public final class OwnerSegment {

    /** Tier for owners at membership level {@value #PREMIUM_MIN_LEVEL} or above. */
    public static final String PREMIUM = "PREMIUM";

    /** Tier for owners below membership level {@value #PREMIUM_MIN_LEVEL}. */
    public static final String STANDARD = "STANDARD";

    /** Area for owners in a known region. */
    public static final String METRO = "METRO";

    /** Area for owners not in a known region. */
    public static final String REGIONAL = "REGIONAL";

    /** Membership level at or above which the {@link #PREMIUM} tier applies. */
    private static final int PREMIUM_MIN_LEVEL = 3;

    private OwnerSegment() {
    }

    /**
     * Returns the segment {@code "<TIER>_<AREA>"} for the given membership level and
     * locality. A {@code null} membership level is treated as {@link #STANDARD}.
     */
    public static String of(Integer membershipLevel, String locality) {
        return tier(membershipLevel) + "_" + area(locality);
    }

    private static String tier(Integer membershipLevel) {
        return membershipLevel != null && membershipLevel >= PREMIUM_MIN_LEVEL ? PREMIUM : STANDARD;
    }

    private static String area(String locality) {
        return Locality.isKnownRegion(locality) ? METRO : REGIONAL;
    }
}
