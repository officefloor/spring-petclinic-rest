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

import java.util.ArrayList;
import java.util.List;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.validation.HouseholdKey;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Finds the existing members of an owner's household. A household is keyed on (last name, postcode)
 * via {@link HouseholdKey}, so the members are simply the already-persisted owners that share the
 * given owner's key. This is the single place that resolves household membership, keeping every
 * household-derived value (size, membership-level ceiling) in agreement.
 */
@Component
public class HouseholdMembers {

    private final ClinicService clinicService;

    private final HouseholdKey householdKey;

    public HouseholdMembers(ClinicService clinicService, HouseholdKey householdKey) {
        this.clinicService = clinicService;
        this.householdKey = householdKey;
    }

    /**
     * @param owner the owner being created, not yet saved
     * @return every existing owner in {@code owner}'s household, i.e. sharing its last name and
     * postcode; empty when {@code owner} is the first member of its household
     */
    public List<Owner> existing(Owner owner) {
        String key = this.householdKey.of(owner.getLastName(), owner.getPostcode());
        List<Owner> members = new ArrayList<>();
        for (Owner existing : this.clinicService.findAllOwners()) {
            if (this.householdKey.of(existing.getLastName(), existing.getPostcode()).equals(key)) {
                members.add(existing);
            }
        }
        return members;
    }
}
