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
 * Thrown when a request to create an owner would exceed the maximum number of owners that may be
 * registered on a single day (100). Once that many owners already carry today's registration date,
 * no further owners may be created today and the request is rejected. Carries the offending date so
 * the REST response can report it.
 */
public class DailyOwnerLimitExceededException extends RuntimeException {

    private final LocalDate date;

    public DailyOwnerLimitExceededException(LocalDate date) {
        super("The maximum number of owners for the day has been reached: " + date);
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }
}
