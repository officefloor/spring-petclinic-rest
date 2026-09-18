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
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Records, on create, how many owners belong to the new owner's household once this create
 * completes: the already-stored owners sharing its {@code householdId} plus the owner itself.
 * <p>
 * The count drives the {@code GOLD} membership tier, so it must be assigned after the
 * household identifier has been resolved (see {@link HouseholdAssigner}) but before the owner
 * is saved. An owner that does not belong to a shared household (no {@code householdId}) is
 * its own single-member household.
 */
@Component
public class HouseholdSizeAssigner {

    private final ClinicService clinicService;

    public HouseholdSizeAssigner(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Assigns {@code owner}'s household size: the number of already-stored owners sharing its
     * {@code householdId}, plus one for the owner being created. Call this after the household
     * identifier has been assigned and before the owner is saved so it does not count itself.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(1);
            return;
        }
        long existing = clinicService.findAllOwners().stream()
            .filter(member -> householdId.equals(member.getHouseholdId()))
            .count();
        owner.setHouseholdSize(Math.toIntExact(existing + 1));
    }
}
