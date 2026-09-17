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

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.error.CityOwnerLimitExceededException;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Rejects creating an owner in a city that has already reached its capacity of {@value #MAX_OWNERS_PER_CITY}
 * owners.
 */
@Component
public class CityOwnerLimitValidator {

    static final long MAX_OWNERS_PER_CITY = 50;

    private final ClinicService clinicService;

    public CityOwnerLimitValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * @param owner the submitted owner fields
     * @throws CityOwnerLimitExceededException if the owner's city already contains
     * {@value #MAX_OWNERS_PER_CITY} or more owners
     */
    public void validate(OwnerFieldsDto owner) {
        if (this.clinicService.countOwnersInCity(owner.getCity()) >= MAX_OWNERS_PER_CITY) {
            throw new CityOwnerLimitExceededException(owner.getCity());
        }
    }
}
