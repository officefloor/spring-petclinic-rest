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

import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Guards owner creation against exact household duplicates. A household is keyed on last name and
 * postcode, but sharing a household is not by itself a duplicate: distinct people in the same
 * household are told apart by their telephone. Only a candidate that matches an existing household
 * member on telephone as well is a true re-registration and is rejected; a household member with a
 * different telephone is a legitimate new member, left to {@link PossibleDuplicateDetector} to flag
 * softly. Setting {@code sharesHousehold} declares the owner an intentional household member and
 * bypasses the block.
 */
@Component
public class HouseholdDuplicateValidator {

    private final Households households;

    public HouseholdDuplicateValidator(Households households) {
        this.households = households;
    }

    /**
     * Reject {@code candidate} when an existing member of its household already carries the same
     * telephone (a true re-registration), unless it declares itself a shared-household member.
     * Household members with a different telephone are legitimate new members and are not rejected.
     *
     * @param candidate       the owner about to be created, with its {@code householdId} and
     *                        normalized telephone already set
     * @param sharesHousehold whether the owner opted into a shared household, bypassing the block
     * @throws HouseholdDuplicateException if a household member shares the candidate's telephone and
     * {@code sharesHousehold} is {@code false}
     */
    public void validate(Owner candidate, boolean sharesHousehold) {
        if (sharesHousehold) {
            return;
        }
        boolean exactDuplicate = households.findMembers(candidate).stream()
            .anyMatch(member -> Objects.equals(member.getTelephone(), candidate.getTelephone()));
        if (exactDuplicate) {
            throw new HouseholdDuplicateException(candidate.getHouseholdId());
        }
    }
}
