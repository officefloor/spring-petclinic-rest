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
 * Decides whether an owner should carry a capacity warning: the flag is raised once the owner's
 * city already holds at least {@value #WARNING_THRESHOLD} owners but is still below the hard
 * {@link CityCapacityValidator#MAX_OWNERS_PER_CITY} limit, signalling that the city is approaching
 * capacity. It reads the same per-city count the hard {@link CityCapacityValidator} rejection uses,
 * only with a lower threshold.
 */
@Component
public class CityCapacityWarningEvaluator {

    /** Number of owners in a city at or above which the approaching-capacity warning is raised. */
    static final int WARNING_THRESHOLD = 40;

    private final ClinicService clinicService;

    public CityCapacityWarningEvaluator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param owner the owner whose city occupancy is checked
     * @return {@code true} when the owner's city holds between {@value #WARNING_THRESHOLD} and
     *         {@link CityCapacityValidator#MAX_OWNERS_PER_CITY} (exclusive) owners, {@code false}
     *         otherwise (including when the owner or its city is not yet known)
     */
    public boolean isWarranted(Owner owner) {
        if (owner == null || owner.getCity() == null) {
            return false;
        }
        long count = clinicService.countOwnersInCity(owner.getCity());
        return count >= WARNING_THRESHOLD && count < CityCapacityValidator.MAX_OWNERS_PER_CITY;
    }
}
