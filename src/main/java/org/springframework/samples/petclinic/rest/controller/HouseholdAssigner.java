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
import org.springframework.stereotype.Component;

/**
 * Tags an owner, on create, with its household's stable shared identifier. The identifier is
 * derived deterministically from the owner's last name and postcode (see
 * {@link HouseholdMatcher}), so every owner in the same household ends up carrying the same
 * value without any explicit linking or back-filling step.
 */
@Component
public class HouseholdAssigner {

    private final HouseholdMatcher householdMatcher;

    public HouseholdAssigner(HouseholdMatcher householdMatcher) {
        this.householdMatcher = householdMatcher;
    }

    /**
     * Tags {@code candidate} with its household's shared identifier. The candidate is not
     * saved here; the caller persists it as part of the create.
     *
     * @param candidate the owner being created
     * @return the shared household identifier applied to the candidate
     */
    public String assign(Owner candidate) {
        String householdId = householdMatcher.householdId(candidate);
        candidate.setHouseholdId(householdId);
        return householdId;
    }
}
