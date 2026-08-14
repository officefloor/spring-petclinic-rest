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

package org.springframework.samples.petclinic.rest.advice;

import java.time.LocalDate;

/**
 * Raised when an owner is created on a day that has already reached the maximum number of owner
 * registrations (100 or more owners sharing the same registrationDate). Maps to a 429 Too Many
 * Requests response.
 */
public class DailyOwnerLimitExceededException extends RuntimeException {

    private final LocalDate registrationDate;

    public DailyOwnerLimitExceededException(LocalDate registrationDate) {
        super("The maximum number of owners for " + registrationDate + " has already been reached");
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }
}
