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
 * Thrown when a new owner is created on a day that already has
 * {@link org.springframework.samples.petclinic.service.ClinicServiceImpl#MAX_OWNERS_PER_DAY} or
 * more owners registered (counted by {@code registrationDate}). Mapped to an HTTP 429 Too Many
 * Requests response by the web layer.
 */
public class DailyRegistrationLimitException extends RuntimeException {

    public DailyRegistrationLimitException(LocalDate registrationDate) {
        super("The maximum number of owners has already been registered on " + registrationDate);
    }

}
