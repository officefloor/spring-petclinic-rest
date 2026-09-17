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

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.BusinessDay;
import org.springframework.samples.petclinic.rest.error.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Rejects creating an owner once {@value #MAX_OWNERS_PER_DAY} or more owners have already been
 * registered on the current day.
 */
@Component
public class DailyOwnerLimitValidator {

    static final long MAX_OWNERS_PER_DAY = 100;

    private final ClinicService clinicService;

    public DailyOwnerLimitValidator(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Enforce the daily create-limit for the business day this owner will be registered on: the
     * supplied date (or the server's current date when none was supplied), rolled forward off
     * weekends to match the date the owner will actually be stored under.
     *
     * @param suppliedRegistrationDate the client-supplied registration date, or {@code null} when
     * none was supplied
     * @throws DailyOwnerLimitExceededException if {@value #MAX_OWNERS_PER_DAY} or more owners have
     * already been registered on that business day
     */
    public void validate(LocalDate suppliedRegistrationDate) {
        LocalDate registrationDate = BusinessDay.onOrAfter(
            suppliedRegistrationDate != null ? suppliedRegistrationDate : LocalDate.now());
        if (this.clinicService.countOwnersRegisteredOn(registrationDate) >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(registrationDate);
        }
    }
}
