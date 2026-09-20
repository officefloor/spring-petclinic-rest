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
 * Guards owner creation against household duplicates. Because a household is keyed on last name
 * and postcode, an owner whose {@link Households#householdId(Owner) household identifier} already
 * belongs to an existing owner is a second member of that household and is rejected. Setting
 * {@code sharesHousehold} declares the owner an intentional household member and bypasses the
 * block.
 */
@Component
public class HouseholdDuplicateValidator {

    private final Households households;

    public HouseholdDuplicateValidator(Households households) {
        this.households = households;
    }

    /**
     * Reject {@code candidate} when it is a second member of an existing household, unless it
     * declares itself a shared-household member.
     *
     * @param candidate       the owner about to be created, with its {@code householdId} already set
     * @param sharesHousehold whether the owner opted into a shared household, bypassing the block
     * @throws HouseholdDuplicateException if another owner already belongs to the candidate's
     * household and {@code sharesHousehold} is {@code false}
     */
    public void validate(Owner candidate, boolean sharesHousehold) {
        if (sharesHousehold) {
            return;
        }
        if (!households.findMembers(candidate).isEmpty()) {
            throw new HouseholdDuplicateException(candidate.getHouseholdId());
        }
    }
}
