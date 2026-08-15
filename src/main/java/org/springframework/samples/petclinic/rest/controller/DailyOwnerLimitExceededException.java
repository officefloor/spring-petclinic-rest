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

/**
 * Raised when a create request would exceed the maximum number of owners permitted to be created in a
 * single day (compared by {@code registrationDate}). Carries the day and the limit so the API can report
 * the rejection back to the client.
 */
public class DailyOwnerLimitExceededException extends RuntimeException {

    private final LocalDate day;

    private final int limit;

    public DailyOwnerLimitExceededException(LocalDate day, int limit) {
        super("The maximum of " + limit + " owners for " + day + " has already been reached");
        this.day = day;
        this.limit = limit;
    }

    public LocalDate getDay() {
        return this.day;
    }

    public int getLimit() {
        return this.limit;
    }
}
