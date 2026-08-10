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
 * Thrown when an owner is created on a day that has already reached the maximum permitted
 * number of owner registrations (100 or more owners sharing the same registration date).
 * <p>
 * The exception handler reports this as a 429 Too Many Requests, since the request exceeds
 * the allowed rate of owner creations for the current day.
 */
public class DailyOwnerLimitException extends RuntimeException {

    private final LocalDate date;

    public DailyOwnerLimitException(LocalDate date) {
        super("The daily owner registration limit for '" + date + "' has been reached");
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }
}
