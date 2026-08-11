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

import java.time.LocalDate;

/**
 * Thrown when a request to create an owner supplies a registration date that is later than the
 * server's current date. A registration date may not be in the future, so the request is rejected.
 * Carries the offending date so the REST response can report it.
 */
public class FutureRegistrationDateException extends RuntimeException {

    private final LocalDate date;

    public FutureRegistrationDateException(LocalDate date) {
        super("The registration date may not be later than the server date: " + date);
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }
}
