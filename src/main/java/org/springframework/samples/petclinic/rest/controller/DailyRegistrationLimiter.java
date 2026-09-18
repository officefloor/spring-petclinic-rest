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
 * Enforces, on create, the maximum number of owners that may be registered on a single day.
 * <p>
 * A day is at capacity once {@link #MAX_OWNERS_PER_DAY} owners already carry it as their
 * registration date, at which point no further owner may be registered on that day. The date
 * checked is the new owner's effective, business-day-adjusted registration date. The count
 * reflects the owners stored before this create, so it must be checked before the owner is saved.
 */
@Component
public class DailyRegistrationLimiter {

    /** Maximum number of owners that may be registered on a single day. */
    static final int MAX_OWNERS_PER_DAY = 100;

    private final ClinicService clinicService;

    public DailyRegistrationLimiter(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Returns whether {@code registrationDate} has already reached its owner-registration
     * capacity, meaning no further owner may be registered on it. Call this before the owner is
     * saved so it does not count itself.
     *
     * @param registrationDate the new owner's effective, business-day-adjusted registration date
     * @return {@code true} if {@link #MAX_OWNERS_PER_DAY} or more owners already carry that date
     */
    public boolean isDailyLimitReached(LocalDate registrationDate) {
        return clinicService.countOwnersByRegistrationDate(registrationDate) >= MAX_OWNERS_PER_DAY;
    }
}
