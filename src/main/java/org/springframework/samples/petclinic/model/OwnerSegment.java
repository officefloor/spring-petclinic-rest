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

import java.util.Set;

/**
 * Derives an owner's marketing segment, formatted {@code '<TIER>_<AREA>'}.
 *
 * <p>The {@code TIER} is {@code PREMIUM} when the owner's membership level is
 * {@value #PREMIUM_MIN_LEVEL} or more, otherwise {@code STANDARD}. The
 * {@code AREA} is {@code METRO} when the owner's locality is one of the known
 * metro regions ({@code NSW}, {@code VIC} or {@code QLD}), otherwise
 * {@code REGIONAL}.
 */
public final class OwnerSegment {

    /** Membership level at or above which an owner is in the {@code PREMIUM} tier. */
    private static final int PREMIUM_MIN_LEVEL = 3;

    /** The localities classified as {@code METRO}; any other locality is {@code REGIONAL}. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /**
     * Return the {@code '<TIER>_<AREA>'} segment for the given membership level and
     * locality.
     *
     * @param membershipLevel the owner's membership level (may be {@code null})
     * @param locality the owner's canonical region (may be {@code null})
     * @return one of {@code "PREMIUM_METRO"}, {@code "PREMIUM_REGIONAL"},
     * {@code "STANDARD_METRO"} or {@code "STANDARD_REGIONAL"}
     */
    public static String of(Integer membershipLevel, String locality) {
        return tier(membershipLevel) + "_" + area(locality);
    }

    private static String tier(Integer membershipLevel) {
        return membershipLevel != null && membershipLevel >= PREMIUM_MIN_LEVEL ? "PREMIUM" : "STANDARD";
    }

    private static String area(String locality) {
        return METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
    }
}
