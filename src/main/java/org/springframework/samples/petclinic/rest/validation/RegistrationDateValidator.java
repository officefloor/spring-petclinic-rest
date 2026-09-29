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

package org.springframework.samples.petclinic.rest.validation;

import java.time.LocalDate;

/**
 * Validates the optional registration date submitted when creating an owner. The date is validated
 * only <em>when present</em>: a {@code null} registration date is left untouched (an owner created
 * without one stays valid and later defaults to the server date). When present it must not fall
 * later than the current server date.
 *
 * <p>A future registration date raises a {@link FutureRegistrationDateException} so the request is
 * rejected with {@code 400 Bad Request}.
 */
public final class RegistrationDateValidator {

    private RegistrationDateValidator() {
    }

    /**
     * Validate the supplied registration date against the current server date.
     *
     * @param registrationDate the supplied registration date (may be {@code null} when absent)
     * @throws FutureRegistrationDateException if the date is present and later than the server date
     */
    public static void validate(LocalDate registrationDate) {
        LocalDate serverDate = LocalDate.now();
        if (registrationDate != null && registrationDate.isAfter(serverDate)) {
            throw new FutureRegistrationDateException(
                "registrationDate must not be later than the server date " + serverDate);
        }
    }
}
