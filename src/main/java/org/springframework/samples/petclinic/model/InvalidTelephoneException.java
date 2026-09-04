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

package org.springframework.samples.petclinic.model;

/**
 * Signals that a telephone value cannot be reduced to a valid E.164 number.
 * Mapped to HTTP 400 Bad Request by the REST exception advice.
 */
public class InvalidTelephoneException extends RuntimeException {

    public InvalidTelephoneException(String telephone) {
        super("Telephone is not a valid E.164 number: " + telephone);
    }
}
