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

import org.springframework.stereotype.Component;

/**
 * Enforces that no more than {@value #MAX_OWNERS_PER_DAY} owners may be registered on a single day.
 * The owners already registered today are counted by their registration date via
 * {@link DailyRegistrationCounter}; once that count reaches the cap, no further owner may be created
 * until the next day.
 */
@Component
public class DailyRegistrationCapacityValidator {

    /** The maximum number of owners that may be registered in a single day. */
    public static final long MAX_OWNERS_PER_DAY = 100;

    private final DailyRegistrationCounter dailyRegistrationCounter;

    public DailyRegistrationCapacityValidator(DailyRegistrationCounter dailyRegistrationCounter) {
        this.dailyRegistrationCounter = dailyRegistrationCounter;
    }

    /**
     * Rejects an owner registration once {@value #MAX_OWNERS_PER_DAY} or more owners have already
     * been registered on the given day.
     *
     * @param day the (business) day the owner is being registered on
     * @throws DailyRegistrationLimitExceededException if that day is already at capacity
     */
    public void validateHasCapacity(LocalDate day) {
        if (this.dailyRegistrationCounter.count(day) >= MAX_OWNERS_PER_DAY) {
            throw new DailyRegistrationLimitExceededException(MAX_OWNERS_PER_DAY);
        }
    }
}
