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

package org.springframework.samples.petclinic.rest.validation;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

/**
 * Validates an optional owner registration date. A supplied date may not be later than the
 * server's current date; an absent (null) date is left unvalidated so it can be defaulted to
 * the current date, keeping the create contract backward-compatible.
 */
@Component
public class RegistrationDateValidator {

    /**
     * @param registrationDate the raw submitted registration date (may be {@code null})
     * @throws FutureRegistrationDateException if the date is present and later than the server's
     *         current date
     */
    public void validate(LocalDate registrationDate) {
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
    }
}
