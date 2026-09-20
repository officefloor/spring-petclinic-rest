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
 * Guards owner creation against duplicate households: two owners are considered to share a
 * household when their last name and address match once compared case-insensitively and with
 * runs of whitespace collapsed to a single space.
 */
@Component
public class HouseholdDuplicateValidator {

    private final Households households;

    public HouseholdDuplicateValidator(Households households) {
        this.households = households;
    }

    /**
     * Reject {@code candidate} when an existing owner already occupies the same household.
     *
     * @param candidate the owner about to be created
     * @throws DuplicateHouseholdException if another owner shares the candidate's last name and
     * address
     */
    public void validate(Owner candidate) {
        if (!households.findMembers(candidate).isEmpty()) {
            throw new DuplicateHouseholdException(candidate.getLastName(), candidate.getAddress());
        }
    }
}
