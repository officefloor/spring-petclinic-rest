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
 * Enrolls an owner into its household when the create deliberately shares one. The joining owner and
 * every existing owner at the same last name and address are stamped with the same stable
 * {@code householdId}, so a whole household carries one shared identifier.
 */
@Component
public class HouseholdRegistrar {

    private final ClinicService clinicService;

    private final HouseholdKey householdKey;

    private final HouseholdIdGenerator householdIdGenerator;

    public HouseholdRegistrar(ClinicService clinicService,
                              HouseholdKey householdKey,
                              HouseholdIdGenerator householdIdGenerator) {
        this.clinicService = clinicService;
        this.householdKey = householdKey;
        this.householdIdGenerator = householdIdGenerator;
    }

    /**
     * Assign the shared household identifier to {@code owner} and back-fill it onto the existing
     * housemates that lack it. The caller is responsible for saving {@code owner} itself.
     *
     * @param owner the owner being created, not yet saved
     * @return the number of members in the household once {@code owner} joins, counting the
     * joining owner and every existing housemate at the same last name and address
     */
    public int register(Owner owner) {
        String householdId = this.householdIdGenerator.generate(owner.getLastName(), owner.getAddress());
        owner.setHouseholdId(householdId);
        String key = this.householdKey.of(owner.getLastName(), owner.getAddress());
        int members = 1;
        for (Owner housemate : this.clinicService.findAllOwners()) {
            if (this.householdKey.of(housemate.getLastName(), housemate.getAddress()).equals(key)) {
                members++;
                if (!householdId.equals(housemate.getHouseholdId())) {
                    housemate.setHouseholdId(householdId);
                    this.clinicService.saveOwner(housemate);
                }
            }
        }
        return members;
    }
}
