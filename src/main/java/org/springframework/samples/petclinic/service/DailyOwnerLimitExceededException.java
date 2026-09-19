/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.service;

import java.time.LocalDate;

/**
 * Thrown when an owner is created on a day that has already reached the maximum number of owner
 * registrations. Signals that the caller has hit a rate limit rather than sent a bad request.
 */
public class DailyOwnerLimitExceededException extends RuntimeException {

    public DailyOwnerLimitExceededException(LocalDate registrationDate, int limit) {
        super("The maximum of " + limit + " owners for " + registrationDate + " has already been reached");
    }
}
