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

package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Decides whether a city is approaching the {@link CityOwnerLimitValidator#MAX_OWNERS_PER_CITY}
 * owner capacity limit: it is once the city already holds at least
 * {@value #WARNING_THRESHOLD} owners while still below the hard limit. The
 * {@value #WARNING_THRESHOLD}-owner threshold is the city's <em>soft capacity</em>.
 */
@Component
public class CityCapacityWarningEvaluator {

    /** The soft capacity: the owner count at which a city starts warning of impending fullness. */
    static final long WARNING_THRESHOLD = 40;

    private final ClinicService clinicService;

    public CityCapacityWarningEvaluator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param city the owner's city
     * @return {@code true} when {@code city} already contains between
     * {@value #WARNING_THRESHOLD} and {@link CityOwnerLimitValidator#MAX_OWNERS_PER_CITY} minus one
     * owners (inclusive), signalling it is approaching the capacity limit; {@code false} otherwise.
     */
    public boolean isApproachingCapacity(String city) {
        long count = this.clinicService.countOwnersInCity(city);
        return count >= WARNING_THRESHOLD && count < CityOwnerLimitValidator.MAX_OWNERS_PER_CITY;
    }

    /**
     * @param city the owner's city
     * @return {@code true} when {@code city} already holds at least {@value #WARNING_THRESHOLD}
     * owners, i.e. it is at or over its soft capacity (whether or not it has also reached the hard
     * limit); {@code false} otherwise.
     */
    public boolean isOverSoftCapacity(String city) {
        return this.clinicService.countOwnersInCity(city) >= WARNING_THRESHOLD;
    }
}
