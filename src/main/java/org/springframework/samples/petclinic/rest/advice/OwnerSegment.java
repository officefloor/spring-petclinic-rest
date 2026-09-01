/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.advice;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code ownerSegment} as '<TIER>_<AREA>': TIER is 'PREMIUM' when the
 * membershipLevel is 3 or more, otherwise 'STANDARD'; AREA is 'METRO' when the locality is a
 * known region (NSW, VIC or QLD), otherwise 'REGIONAL'. Kept as a small standalone unit so the
 * response mapper can expose the value without growing.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /** Known metro regions. */
    private static final Set<String> METRO = Set.of("NSW", "VIC", "QLD");

    public static String of(Owner owner) {
        String tier = OwnerMembershipLevel.of(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO.contains(OwnerMemberId.region(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
