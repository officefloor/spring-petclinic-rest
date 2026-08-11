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
 * Thrown when a supplied owner registrationDate is later than the current server date. A
 * registration date may be omitted (it then defaults to the server date) or set to today or any
 * past day, but it may never lie in the future. Carries the offending value so it can be reported
 * to the client.
 */
public class FutureRegistrationDateException extends RuntimeException {

    private final LocalDate rejectedValue;

    public FutureRegistrationDateException(LocalDate rejectedValue) {
        super("Registration date '" + rejectedValue + "' is later than the current server date");
        this.rejectedValue = rejectedValue;
    }

    public LocalDate getRejectedValue() {
        return this.rejectedValue;
    }
}
