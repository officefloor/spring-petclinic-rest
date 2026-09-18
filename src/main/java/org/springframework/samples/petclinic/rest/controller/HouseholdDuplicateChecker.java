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
 * Detects, on create, whether an owner would duplicate an existing household, i.e. another
 * owner already shares its last name and address (see {@link HouseholdMatcher} for how
 * household membership is determined).
 */
@Component
public class HouseholdDuplicateChecker {

    private final HouseholdMatcher householdMatcher;

    public HouseholdDuplicateChecker(HouseholdMatcher householdMatcher) {
        this.householdMatcher = householdMatcher;
    }

    /**
     * Reports whether an already-stored owner shares {@code candidate}'s household.
     *
     * @param candidate the owner being created
     * @return {@code true} when another owner already occupies the same household
     */
    public boolean isDuplicate(Owner candidate) {
        return !householdMatcher.findMembers(candidate).isEmpty();
    }
}
