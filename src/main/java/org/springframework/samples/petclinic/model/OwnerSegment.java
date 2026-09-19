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
 * Classification of an owner into a marketing segment formatted {@code <TIER>_<AREA>}. The tier is
 * {@code PREMIUM} for a membership level of {@value #PREMIUM_MIN_LEVEL} or more, otherwise
 * {@code STANDARD}; the area is {@code METRO} when the owner's locality is a known region, otherwise
 * {@code REGIONAL}.
 */
public final class OwnerSegment {

    /** The membership level at or above which an owner is in the {@code PREMIUM} tier. */
    private static final int PREMIUM_MIN_LEVEL = 3;

    private OwnerSegment() {
    }

    /**
     * Return the segment for the given membership level and locality, formatted {@code <TIER>_<AREA>}.
     *
     * @param membershipLevel the owner's membership level
     * @param locality        the owner's locality (canonical region, or {@code UNKNOWN})
     * @return the segment string, one of {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
     *         {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}
     */
    public static String of(int membershipLevel, String locality) {
        return tier(membershipLevel) + "_" + area(locality);
    }

    private static String tier(int membershipLevel) {
        return membershipLevel >= PREMIUM_MIN_LEVEL ? "PREMIUM" : "STANDARD";
    }

    private static String area(String locality) {
        return LocalityResolver.isKnownRegion(locality) ? "METRO" : "REGIONAL";
    }
}
