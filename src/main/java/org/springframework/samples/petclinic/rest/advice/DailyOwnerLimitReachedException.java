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

package org.springframework.samples.petclinic.rest.advice;

import java.time.LocalDate;

/**
 * Thrown when an owner is created on a day that has already reached the maximum number of
 * owners that may be registered in a single day (100, counted by {@code registrationDate}).
 * The daily quota is exhausted, so the create is rejected with a {@code 429 Too Many Requests}
 * response.
 */
public class DailyOwnerLimitReachedException extends RuntimeException {

    private final transient LocalDate date;

    public DailyOwnerLimitReachedException(LocalDate date) {
        super("The maximum number of owners has already been created on " + date);
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }
}
