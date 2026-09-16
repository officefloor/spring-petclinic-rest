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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Rejects an owner that would silently join an existing household. Because a household is keyed on
 * (last name, postcode) through its {@link HouseholdKey#idFor(String, String) household id}, two
 * owners sharing that id belong to the same household; the second one is a household duplicate. The
 * caller bypasses this block only when the new owner explicitly opts in via {@code sharesHousehold},
 * declaring itself a member rather than a duplicate.
 */
@Component
public class HouseholdDuplicateValidator {

    private final ClinicService clinicService;

    public HouseholdDuplicateValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Rejects an owner whose household id already belongs to an existing owner. The owner's
     * household id must already be assigned when this runs.
     *
     * @param owner the owner being created
     * @throws DuplicateHouseholdException if another owner already belongs to this household
     */
    public void rejectIfHouseholdExists(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        boolean duplicate = this.clinicService.findAllOwners().stream()
            .filter(existing -> !existing.isDeleted())
            .anyMatch(existing -> householdId.equals(existing.getHouseholdId()));
        if (duplicate) {
            throw new DuplicateHouseholdException(householdId);
        }
    }
}
