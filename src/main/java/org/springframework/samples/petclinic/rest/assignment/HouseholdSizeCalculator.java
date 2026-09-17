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

package org.springframework.samples.petclinic.rest.assignment;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.validation.HouseholdKey;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Computes the size of an owner's household. Because the household is keyed on (last name, postcode)
 * every owner already carries the shared {@code householdId}, so there is nothing to link: the size
 * is simply the joining owner plus every existing owner in the same {@link HouseholdKey household}.
 */
@Component
public class HouseholdSizeCalculator {

    private final ClinicService clinicService;

    private final HouseholdKey householdKey;

    public HouseholdSizeCalculator(ClinicService clinicService, HouseholdKey householdKey) {
        this.clinicService = clinicService;
        this.householdKey = householdKey;
    }

    /**
     * @param owner the owner being created, not yet saved
     * @return the number of members in the household once {@code owner} joins, counting the joining
     * owner and every existing owner with the same last name and postcode
     */
    public int size(Owner owner) {
        String key = this.householdKey.of(owner.getLastName(), owner.getPostcode());
        int members = 1;
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (this.householdKey.of(existing.getLastName(), existing.getPostcode()).equals(key)) {
                members++;
            }
        }
        return members;
    }
}
