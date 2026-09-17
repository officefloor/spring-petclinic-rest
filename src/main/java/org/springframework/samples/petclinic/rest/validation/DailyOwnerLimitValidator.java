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
     * @throws DailyOwnerLimitExceededException if {@value #MAX_OWNERS_PER_DAY} or more owners have
     * already been registered today
     */
    public void validate() {
        LocalDate today = LocalDate.now();
        if (this.clinicService.countOwnersRegisteredOn(today) >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(today);
        }
    }
}
