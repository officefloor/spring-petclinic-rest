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
 * Derives an owner's {@code ownerSegment}, formatted {@code <TIER>_<AREA>}: TIER is
 * {@code PREMIUM} at membership level 3 or more else {@code STANDARD}, and AREA is
 * {@code METRO} for a known region (NSW, VIC or QLD) else {@code REGIONAL}.
 */
public final class OwnerSegment {

    private static final Set<String> KNOWN_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /**
     * @param membershipLevel the owner's numeric membership level
     * @param locality        the owner's region
     * @return the segment, e.g. {@code STANDARD_METRO}
     */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = KNOWN_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
