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

import java.time.LocalDate;

import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Guards owner creation against overloading a single day: at most
 * {@value #MAX_OWNERS_PER_DAY} owners may be registered on any given date, so a new owner is
 * rejected once that many have already been registered today.
 */
@Component
public class DailyRegistrationLimitValidator {

    /** Maximum number of owners allowed to be registered on a single day. */
    static final int MAX_OWNERS_PER_DAY = 100;

    private final ClinicService clinicService;

    public DailyRegistrationLimitValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Reject owner creation when the registrations already booked for the given business day
     * have reached the daily limit.
     *
     * @param registrationDate the (business-day adjusted) date the new owner would be registered on
     * @throws DailyRegistrationLimitExceededException if the daily limit has been reached
     */
    public void validate(LocalDate registrationDate) {
        if (clinicService.countOwnersRegisteredOn(registrationDate) >= MAX_OWNERS_PER_DAY) {
            throw new DailyRegistrationLimitExceededException(MAX_OWNERS_PER_DAY);
        }
    }
}
