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
 * Decides whether the "bulk signup" warning applies: it does once more than
 * {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners have already been registered on a single day.
 * The owners registered on the day are counted by their registration date via
 * {@link DailyRegistrationCounter}, the same accumulation path used to enforce the per-day cap in
 * {@link DailyRegistrationCapacityValidator}.
 */
@Component
public class BulkSignupWarningEvaluator {

    /** The number of owners a day must exceed before the bulk-signup warning is raised. */
    public static final long BULK_SIGNUP_WARNING_THRESHOLD = 80;

    private final DailyRegistrationCounter dailyRegistrationCounter;

    public BulkSignupWarningEvaluator(DailyRegistrationCounter dailyRegistrationCounter) {
        this.dailyRegistrationCounter = dailyRegistrationCounter;
    }

    /**
     * @return {@code true} when more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners have already
     * been registered today.
     */
    public boolean isWarranted() {
        return isWarranted(LocalDate.now());
    }

    /**
     * @param day the day to evaluate registrations for
     * @return {@code true} when more than {@value #BULK_SIGNUP_WARNING_THRESHOLD} owners have already
     * been registered on the given day.
     */
    public boolean isWarranted(LocalDate day) {
        return this.dailyRegistrationCounter.count(day) > BULK_SIGNUP_WARNING_THRESHOLD;
    }
}
