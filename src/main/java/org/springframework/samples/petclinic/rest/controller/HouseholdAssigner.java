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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Assigns owners who deliberately share a household (via the {@code sharesHousehold} flag)
 * the household's stable shared identifier, back-filling any existing member that does not
 * yet carry it so the whole household ends up tagged with the same value.
 */
@Component
public class HouseholdAssigner {

    private final HouseholdMatcher householdMatcher;

    private final ClinicService clinicService;

    public HouseholdAssigner(HouseholdMatcher householdMatcher, ClinicService clinicService) {
        this.householdMatcher = householdMatcher;
        this.clinicService = clinicService;
    }

    /**
     * Tags {@code candidate} with its household's shared identifier and back-fills the same
     * value onto existing members that lack one. The candidate is not saved here; the
     * caller persists it as part of the create.
     *
     * @param candidate the owner being created into an existing household
     * @return the shared household identifier applied to the candidate
     */
    public String assign(Owner candidate) {
        String householdId = householdMatcher.householdId(candidate);
        candidate.setHouseholdId(householdId);
        for (Owner member : householdMatcher.findMembers(candidate)) {
            if (member.getHouseholdId() == null) {
                member.setHouseholdId(householdId);
                clinicService.saveOwner(member);
            }
        }
        return householdId;
    }
}
