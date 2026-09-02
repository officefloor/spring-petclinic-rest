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

import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's segment as '<TIER>_<AREA>': TIER is 'PREMIUM' at membershipLevel 3 or more,
 * otherwise 'STANDARD'; AREA is 'METRO' when the locality is a known region (NSW, VIC or QLD),
 * otherwise 'REGIONAL'. Read off the already-derived response fields, so it never touches storage.
 */
final class OwnerSegment {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    static String of(OwnerDto owner) {
        Integer level = owner.getMembershipLevel();
        String tier = level != null && level >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(owner.getLocality()) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
