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

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's segment, formatted {@code <TIER>_<AREA>}. TIER is {@code PREMIUM}
 * when the {@link MembershipLevel} is 3 or more, otherwise {@code STANDARD}. AREA is
 * {@code METRO} when the {@link CustomerCode#regionOf(Owner) locality} is a known region
 * (NSW, VIC or QLD), otherwise {@code REGIONAL}. The four possible values are therefore
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} and
 * {@code STANDARD_REGIONAL}.
 */
public final class OwnerSegment {

    /** Membership level at or above which the owner is in the PREMIUM tier. */
    private static final int PREMIUM_LEVEL = 3;

    /** Regions treated as METRO areas. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /**
     * @param owner the owner whose segment to derive.
     * @return the segment formatted {@code <TIER>_<AREA>}.
     */
    public static String of(Owner owner) {
        String tier = MembershipLevel.of(owner) >= PREMIUM_LEVEL ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(CustomerCode.regionOf(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
