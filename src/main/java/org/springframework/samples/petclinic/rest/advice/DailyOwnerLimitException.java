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
 * Thrown by REST controllers when an owner cannot be created because the maximum number of owners
 * that may be registered on a single day (100) has already been reached for that day (compared by
 * {@code registrationDate}). The exception handler reports it as a {@code 429 Too Many Requests}.
 */
public class DailyOwnerLimitException extends RuntimeException {

    public DailyOwnerLimitException(LocalDate registrationDate) {
        super("The maximum number of owners for '" + registrationDate + "' has already been reached");
    }
}
