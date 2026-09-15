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

/**
 * Resolves an owner's marketing segment, formatted {@code '<TIER>_<AREA>'}. The tier is
 * {@code PREMIUM} once the membership level reaches {@link #PREMIUM_MIN_LEVEL}, otherwise
 * {@code STANDARD}. The area is {@code METRO} when the locality is a known canonical region
 * (NSW, VIC or QLD per {@link LocalityResolver#isKnownRegion(String)}), otherwise
 * {@code REGIONAL}.
 */
public abstract class OwnerSegmentResolver {

    /** Lowest membership level that earns the {@code PREMIUM} tier. */
    public static final int PREMIUM_MIN_LEVEL = 3;

    /**
     * Return the {@code '<TIER>_<AREA>'} segment for the given membership level and locality:
     * one of {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
     * {@code STANDARD_REGIONAL}.
     */
    public static String segmentOf(int membershipLevel, String locality) {
        String tier = membershipLevel >= PREMIUM_MIN_LEVEL ? "PREMIUM" : "STANDARD";
        String area = LocalityResolver.isKnownRegion(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

}
