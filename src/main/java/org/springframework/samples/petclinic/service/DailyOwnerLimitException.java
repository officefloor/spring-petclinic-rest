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
package org.springframework.samples.petclinic.service;

import java.time.LocalDate;

/**
 * Thrown when an {@link org.springframework.samples.petclinic.model.Owner} is created on a day
 * that already has the maximum number of owners registered (100). Signals that the daily create
 * quota has been exhausted, mapped to HTTP 429.
 */
public class DailyOwnerLimitException extends RuntimeException {

    public DailyOwnerLimitException(LocalDate date, long limit) {
        super("Day '" + date + "' already has the maximum of " + limit + " owners registered");
    }
}
