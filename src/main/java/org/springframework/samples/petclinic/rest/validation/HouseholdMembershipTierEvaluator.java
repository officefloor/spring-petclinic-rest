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
 * Decides whether an owner earns the household-based {@code GOLD} membership tier: it does once the
 * owner's household — the owners sharing the same
 * {@link org.springframework.samples.petclinic.model.Owner#getHouseholdId() householdId} — has at
 * least {@value #GOLD_HOUSEHOLD_SIZE} members. This household-aware upgrade supersedes the
 * individual {@code BRONZE}/{@code SILVER} tier that {@link Owner#getMembershipTier()} derives from a
 * single owner's own fields; it is evaluated per response because it depends on how many owners
 * currently share the household. Owners without a household id never qualify.
 */
@Component
public class HouseholdMembershipTierEvaluator {

    /** The membership tier granted to sufficiently large households. */
    public static final String GOLD_TIER = "GOLD";

    /** The number of household members at (or above) which the {@link #GOLD_TIER} applies. */
    public static final long GOLD_HOUSEHOLD_SIZE = 3;

    private final ClinicService clinicService;

    public HouseholdMembershipTierEvaluator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param householdId the household id to evaluate, or {@code null} for an owner without a household
     * @return {@code true} when at least {@value #GOLD_HOUSEHOLD_SIZE} owners share the given household id
     */
    public boolean qualifiesForGold(String householdId) {
        if (householdId == null) {
            return false;
        }
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> householdId.equals(existing.getHouseholdId()))
            .count() >= GOLD_HOUSEHOLD_SIZE;
    }
}
