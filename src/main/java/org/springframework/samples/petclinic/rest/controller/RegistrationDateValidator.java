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

package org.springframework.samples.petclinic.rest.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

/**
 * Guards owner creation against back-dated-in-reverse registrations: a supplied registration
 * date may not lie in the future relative to the server's current date.
 */
@Component
public class RegistrationDateValidator {

    /**
     * Reject a supplied registration date that is later than the server's current date. A
     * {@code null} date is left for the caller to default and is not rejected here.
     *
     * @param registrationDate the registration date supplied on the request, or {@code null}
     * @throws FutureRegistrationDateException if the supplied date is later than today
     */
    public void validate(LocalDate registrationDate) {
        LocalDate serverDate = LocalDate.now();
        if (registrationDate != null && registrationDate.isAfter(serverDate)) {
            throw new FutureRegistrationDateException(registrationDate, serverDate);
        }
    }
}
