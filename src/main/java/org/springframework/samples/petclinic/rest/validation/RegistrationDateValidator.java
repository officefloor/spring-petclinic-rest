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
 * Validates an owner's optional registration date. A supplied date may not lie in the future: it
 * must not be later than the server's current date. A registration date is optional, so an absent
 * value defaults elsewhere and is accepted here.
 */
public final class RegistrationDateValidator {

    private RegistrationDateValidator() {
    }

    /**
     * Rejects a registration date that is later than today on the server. Registration date is
     * optional, so a {@code null} value is accepted.
     *
     * @param registrationDate the submitted registration date, or {@code null} if none was provided
     * @throws FutureRegistrationDateException if a value is present and later than the server date
     */
    public static void validateNotFuture(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                "Registration date '" + registrationDate + "' is later than the current server date");
        }
    }
}
