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
 * Thrown when an owner is saved with a supplied {@code registrationDate} that is later than the
 * current server date. A registration date may not be in the future. Mapped to an HTTP 400 Bad
 * Request response by the web layer.
 */
public class FutureRegistrationDateException extends RuntimeException {

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("The registration date " + registrationDate + " may not be later than the current date");
    }

}
