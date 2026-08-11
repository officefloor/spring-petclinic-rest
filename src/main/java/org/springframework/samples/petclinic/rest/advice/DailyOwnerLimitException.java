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
 * Thrown when an owner cannot be created because the maximum number of owners for the current day
 * has already been reached. Carries the day whose quota was exhausted so it can be reported.
 */
public class DailyOwnerLimitException extends RuntimeException {

    private final LocalDate date;

    public DailyOwnerLimitException(LocalDate date) {
        super("The maximum number of owners for the day has already been reached: " + date);
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }
}
