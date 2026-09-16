/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.rest.validation;

import java.util.OptionalInt;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Computes the membership-level ceiling for a new owner joining a household: a new owner's level may
 * not exceed one above the highest level among the household members that already exist. The members
 * are the non-deleted owners sharing the new owner's {@link HouseholdKey household id}, the same way
 * {@link HouseholdSizeCounter} and {@link HouseholdDuplicateValidator} identify a household. The
 * ceiling is a snapshot taken at creation time; when no household member exists yet, there is no
 * ceiling.
 */
@Component
public class HouseholdMembershipLevelCap {

    private final ClinicService clinicService;

    public HouseholdMembershipLevelCap(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * The membership-level ceiling for an owner being created into the household identified by
     * {@code householdId}: one above the highest {@link Owner#getMembershipLevel() membership level}
     * among the existing non-deleted members of that household.
     *
     * @param householdId the household id of the owner being created
     * @return the ceiling to apply, or {@code null} when no existing household member caps the owner
     */
    public Integer ceilingFor(String householdId) {
        if (householdId == null) {
            return null;
        }
        OptionalInt highest = this.clinicService.findAllOwners().stream()
            .filter(member -> !member.isDeleted())
            .filter(member -> householdId.equals(member.getHouseholdId()))
            .mapToInt(Owner::getMembershipLevel)
            .max();
        return highest.isPresent() ? highest.getAsInt() + 1 : null;
    }
}
