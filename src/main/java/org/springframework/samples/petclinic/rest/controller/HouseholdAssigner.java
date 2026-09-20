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

package org.springframework.samples.petclinic.rest.controller;

import java.util.List;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Joins an owner that opts into a shared household into the household of any existing owner at
 * the same address, giving every member the same stable {@code householdId}.
 */
@Component
public class HouseholdAssigner {

    private final Households households;

    private final ClinicService clinicService;

    public HouseholdAssigner(Households households, ClinicService clinicService) {
        this.households = households;
        this.clinicService = clinicService;
    }

    /**
     * Assign {@code candidate} the household identifier it shares with any existing same-household
     * owners, back-filling that identifier onto members that do not yet carry it. A no-op when no
     * existing owner shares the candidate's household.
     *
     * @param candidate the owner about to be created; its {@code householdId} is set in place
     */
    public void assignSharedHousehold(Owner candidate) {
        List<Owner> members = households.findMembers(candidate);
        if (members.isEmpty()) {
            return;
        }
        String householdId = households.stableId(candidate);
        candidate.setHouseholdId(householdId);
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                clinicService.saveOwner(member);
            }
        }
    }
}
