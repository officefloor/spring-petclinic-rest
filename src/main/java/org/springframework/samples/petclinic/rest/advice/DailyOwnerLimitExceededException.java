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
 * Thrown when a request tries to create an owner once the maximum number of owners
 * that may be registered on a single day (100 or more, by {@code registrationDate})
 * has already been reached.
 * <p>
 * Signals that the daily create limit has been exhausted so the
 * {@link ExceptionControllerAdvice} can surface it to the client as a
 * {@code 429 Too Many Requests} response.
 */
public class DailyOwnerLimitExceededException extends RuntimeException {

    public DailyOwnerLimitExceededException(LocalDate date) {
        super("The maximum number of owners has already been created on " + date);
    }
}
