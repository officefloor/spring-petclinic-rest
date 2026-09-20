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

package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Derives an owner's membership tier:
 * <ul>
 *   <li>{@code "GOLD"} when the owner's household (owners sharing the same
 *       {@code householdId}) has {@value #GOLD_HOUSEHOLD_SIZE} or more members;</li>
 *   <li>otherwise {@code "SILVER"} when the owner has no namesakes
 *       ({@code namesakeCount} is 0) and an email address on file;</li>
 *   <li>otherwise {@code "BRONZE"}.</li>
 * </ul>
 */
@Component
public class MembershipTierEvaluator {

    /** Number of household members at or above which the owner qualifies for GOLD. */
    static final int GOLD_HOUSEHOLD_SIZE = 3;

    private final ClinicService clinicService;

    public MembershipTierEvaluator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param owner the owner whose tier is evaluated
     * @return the owner's membership tier, or {@code null} when the owner is not known
     */
    public String tierFor(Owner owner) {
        if (owner == null) {
            return null;
        }
        if (isGoldHousehold(owner)) {
            return "GOLD";
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        boolean hasNoNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        return hasNoNamesakes && hasEmail ? "SILVER" : "BRONZE";
    }

    private boolean isGoldHousehold(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return false;
        }
        return clinicService.countOwnersInHousehold(householdId) >= GOLD_HOUSEHOLD_SIZE;
    }
}
