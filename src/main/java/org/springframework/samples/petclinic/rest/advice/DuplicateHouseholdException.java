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

/**
 * Thrown by REST controllers when an owner cannot be created because another owner already has the
 * same last name and address (compared case-insensitively with collapsed whitespace) and the request
 * did not opt in via {@code sharesHousehold}. The exception handler reports it as a
 * {@code 409 Conflict}.
 */
public class DuplicateHouseholdException extends RuntimeException {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with last name '" + lastName + "' already exists at address '" + address + "'");
    }
}
