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

/**
 * Thrown when creating an owner would exceed the maximum number of owners that may be
 * registered in a single day (100 or more owners already registered today, by
 * {@code registrationDate}). Maps to a 429 Too Many Requests response.
 */
public class DailyOwnerLimitException extends RuntimeException {

    public DailyOwnerLimitException(int limit) {
        super("The maximum number of " + limit + " owners for today has already been reached");
    }
}
