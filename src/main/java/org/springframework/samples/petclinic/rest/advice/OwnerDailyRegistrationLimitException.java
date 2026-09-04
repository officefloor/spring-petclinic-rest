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
import java.util.List;

/**
 * Thrown when a request to create an owner would exceed the number of owners that may be registered in
 * a single day - that is, the maximum permitted number of owners have already been created today (by
 * {@code registrationDate}). Unlike the {@link DuplicateOwnerException} family this is a rate limit
 * rather than a uniqueness collision, so it is reported to the client as a {@code 429 Too Many
 * Requests}. Carries the offending date and the client-facing detail so the
 * {@link ExceptionControllerAdvice} can render the response.
 */
public class OwnerDailyRegistrationLimitException extends RuntimeException {

    private static final String DETAIL = "The maximum number of owners for today has already been reached";

    private final LocalDate date;

    public OwnerDailyRegistrationLimitException(LocalDate date) {
        super("Daily owner registration limit reached for: " + date);
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getDetail() {
        return DETAIL;
    }

    public List<String> getErrorFields() {
        return List.of("registrationDate");
    }
}
