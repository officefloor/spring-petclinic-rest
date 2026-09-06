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
 * Thrown when a request to create an owner would exceed the number of owners that may be registered
 * on a single day. Once 100 or more owners share today's {@code registrationDate}, the day is
 * considered full, so the {@link ExceptionControllerAdvice} reports this back to the client as a
 * 429 Too Many Requests.
 */
public class OwnerDailyLimitException extends RuntimeException {

    private final LocalDate registrationDate;

    public OwnerDailyLimitException(LocalDate registrationDate) {
        super("The maximum number of owners has already been created today: " + registrationDate);
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }

}
