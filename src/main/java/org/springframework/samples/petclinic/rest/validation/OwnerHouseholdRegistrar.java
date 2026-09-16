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

import java.util.List;
import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Assigns the shared household identifier when an owner opts in via {@code sharesHousehold}. When
 * the new owner joins an existing household (an owner with the same last name and address already
 * exists), every member of that household — the existing owners and the joining one — is given the
 * same stable {@link HouseholdKey#idFor(String) household id}. If no such household exists yet the
 * new owner is left without one.
 *
 * @see OwnerIdentityUniquenessValidator
 */
@Component
public class OwnerHouseholdRegistrar {

    private final ClinicService clinicService;

    public OwnerHouseholdRegistrar(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Places the given (not-yet-saved) owner into its household, sharing a single identifier with
     * any existing owners at the same last name and address and backfilling that identifier onto
     * members that lack it.
     *
     * @param owner the owner being created with {@code sharesHousehold} set
     */
    public void assignHousehold(Owner owner) {
        String key = HouseholdKey.of(owner.getLastName(), owner.getAddress());
        List<Owner> members = this.clinicService.findAllOwners().stream()
            .filter(existing -> HouseholdKey.of(existing.getLastName(), existing.getAddress()).equals(key))
            .toList();
        if (members.isEmpty()) {
            return;
        }
        String householdId = members.stream()
            .map(Owner::getHouseholdId)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(() -> HouseholdKey.idFor(key));
        for (Owner member : members) {
            if (!householdId.equals(member.getHouseholdId())) {
                member.setHouseholdId(householdId);
                this.clinicService.saveOwner(member);
            }
        }
        owner.setHouseholdId(householdId);
    }
}
