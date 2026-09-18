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
 * Enforces, on create, the limit of owners allowed per city and flags owners created while
 * their city is approaching that limit.
 * <p>
 * A city is at capacity once it already contains {@link #MAX_OWNERS_PER_CITY} owners, at
 * which point no further owner may be created there. It is approaching capacity once it holds
 * at least {@link #CAPACITY_WARNING_THRESHOLD} (but fewer than {@link #MAX_OWNERS_PER_CITY})
 * owners. Both counts reflect the owners stored before this create, so they must be evaluated
 * before the owner is saved.
 */
@Component
public class CityCapacityChecker {

    /** Maximum number of owners a single city may contain. */
    static final int MAX_OWNERS_PER_CITY = 50;

    /** Number of owners at which a city is considered to be approaching capacity. */
    static final int CAPACITY_WARNING_THRESHOLD = 40;

    private final ClinicService clinicService;

    public CityCapacityChecker(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Returns whether {@code owner}'s city has already reached its owner capacity, meaning
     * the owner cannot be created there. Call this before the owner is saved so it does not
     * count itself.
     *
     * @param owner the owner being created
     * @return {@code true} if the city already contains the maximum number of owners
     */
    public boolean isCityAtCapacity(Owner owner) {
        return countOwnersInCity(owner) >= MAX_OWNERS_PER_CITY;
    }

    /**
     * Assigns {@code owner}'s capacity warning: {@code true} when its city already holds
     * between {@link #CAPACITY_WARNING_THRESHOLD} and {@link #MAX_OWNERS_PER_CITY} (exclusive)
     * owners, otherwise {@code false}. Call this before the owner is saved so it does not
     * count itself.
     *
     * @param owner the owner being created
     */
    public void assignCapacityWarning(Owner owner) {
        long ownersInCity = countOwnersInCity(owner);
        owner.setCapacityWarning(
            ownersInCity >= CAPACITY_WARNING_THRESHOLD && ownersInCity < MAX_OWNERS_PER_CITY);
    }

    private long countOwnersInCity(Owner owner) {
        return clinicService.countOwnersByCity(owner.getCity());
    }
}
