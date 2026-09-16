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

import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Counts how many owners belong to a household, i.e. share a given {@link HouseholdKey household id},
 * including the owner being created. Owners with the same last name and postcode derive the same
 * household id (see {@link HouseholdKey}), so this size is the number of existing members plus the
 * new one.
 */
@Component
public class HouseholdSizeCounter {

    private final ClinicService clinicService;

    public HouseholdSizeCounter(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Counts the members of the household identified by {@code householdId}, including the owner
     * being created. Returns 1 when no household id is known, since the owner then stands alone.
     *
     * @param householdId the household id of the owner being created
     * @return the number of existing owners sharing that household id, plus one for the new owner
     */
    public int count(String householdId) {
        if (householdId == null) {
            return 1;
        }
        long existing = this.clinicService.findAllOwners().stream()
            .filter(owner -> householdId.equals(owner.getHouseholdId()))
            .count();
        return (int) existing + 1;
    }
}
