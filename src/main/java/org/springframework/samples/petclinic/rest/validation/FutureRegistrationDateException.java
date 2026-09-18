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

/**
 * Raised when a supplied registration date is later than the server's current date, so the
 * client can be told the value is invalid.
 */
public class FutureRegistrationDateException extends RuntimeException {

    private final LocalDate registrationDate;

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("Registration date " + registrationDate + " must not be later than the server date");
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }
}
