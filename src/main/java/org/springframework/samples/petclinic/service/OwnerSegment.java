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
package org.springframework.samples.petclinic.service;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's segment as '&lt;TIER&gt;_&lt;AREA&gt;': TIER is 'PREMIUM' when the effective
 * membershipLevel is 3 or more, otherwise 'STANDARD'; AREA is 'METRO' when the locality is a
 * known region (NSW, VIC or QLD), otherwise 'REGIONAL'.
 */
public final class OwnerSegment {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    public static String of(Owner owner) {
        Integer level = owner.getMembershipLevel();
        int effective = level != null ? level : MembershipLevel.of(owner);
        String tier = effective >= 3 ? "PREMIUM" : "STANDARD";
        String code = owner.getCustomerCode();
        String region = code.substring(0, code.indexOf('-'));
        String area = METRO_REGIONS.contains(region) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
