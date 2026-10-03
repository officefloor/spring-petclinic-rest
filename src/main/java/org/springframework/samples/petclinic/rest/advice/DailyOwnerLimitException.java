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
 * Thrown when a request to create an owner would exceed the maximum number of owners that may be
 * registered on a single day (100). Carries the day whose quota is exhausted so the
 * {@link ExceptionControllerAdvice} can report it to the client, which answers with a
 * 429 Too Many Requests.
 */
public class DailyOwnerLimitException extends RuntimeException {

    private final transient LocalDate date;

    public DailyOwnerLimitException(LocalDate date) {
        super("Daily owner registration limit reached for: " + date);
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }
}
