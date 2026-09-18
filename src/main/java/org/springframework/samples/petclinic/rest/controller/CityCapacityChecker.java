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
 * Enforces, on create, the limit of owners allowed per city.
 * <p>
 * A city is at capacity once it already contains {@link #MAX_OWNERS_PER_CITY} owners, at
 * which point no further owner may be created there. The count reflects the owners stored
 * before this create, so it must be checked before the owner is saved.
 */
@Component
public class CityCapacityChecker {

    /** Maximum number of owners a single city may contain. */
    static final int MAX_OWNERS_PER_CITY = 50;

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
        return clinicService.countOwnersByCity(owner.getCity()) >= MAX_OWNERS_PER_CITY;
    }
}
