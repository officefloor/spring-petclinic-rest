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

import org.springframework.samples.petclinic.rest.error.FutureRegistrationDateException;
import org.springframework.stereotype.Component;

/**
 * Rejects a client-supplied registration date that lies in the future, so an owner can only be
 * registered on or before the server's current date.
 */
@Component
public class RegistrationDateValidator {

    /**
     * @param suppliedRegistrationDate the client-supplied registration date, or {@code null} when
     * none was supplied
     * @throws FutureRegistrationDateException if the supplied date is later than the server's
     * current date
     */
    public void validate(LocalDate suppliedRegistrationDate) {
        if (suppliedRegistrationDate != null && suppliedRegistrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(suppliedRegistrationDate);
        }
    }
}
