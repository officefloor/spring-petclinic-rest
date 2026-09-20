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
 * Guards owner creation against overfilling a city: a city may hold at most
 * {@value #MAX_OWNERS_PER_CITY} owners, so a candidate whose city already contains that
 * many is rejected.
 */
@Component
public class CityCapacityValidator {

    /** Maximum number of owners allowed to share a single city. */
    static final int MAX_OWNERS_PER_CITY = 50;

    private final ClinicService clinicService;

    public CityCapacityValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Reject {@code candidate} when its city already contains the maximum number of owners.
     *
     * @param candidate the owner about to be created
     * @throws CityCapacityExceededException if the candidate's city is already at capacity
     */
    public void validate(Owner candidate) {
        if (clinicService.countOwnersInCity(candidate.getCity()) >= MAX_OWNERS_PER_CITY) {
            throw new CityCapacityExceededException(candidate.getCity(), MAX_OWNERS_PER_CITY);
        }
    }
}
