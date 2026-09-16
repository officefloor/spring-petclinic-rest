/*
 * Copyright 2016-2017 the original author or authors.
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

import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Counts how many owners have been registered on a given day, compared by their
 * {@link org.springframework.samples.petclinic.model.Owner#getRegistrationDate() registrationDate}.
 * Used to enforce the per-day cap on owner creation via {@link DailyRegistrationCapacityValidator}.
 */
@Component
public class DailyRegistrationCounter {

    private final ClinicService clinicService;

    public DailyRegistrationCounter(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Counts the existing owners whose registration date matches the given day.
     *
     * @param day the day to count registrations for
     * @return the number of existing owners registered on that day
     */
    public long count(LocalDate day) {
        return this.clinicService.findAllOwners().stream()
            .filter(existing -> day.equals(existing.getRegistrationDate()))
            .count();
    }
}
