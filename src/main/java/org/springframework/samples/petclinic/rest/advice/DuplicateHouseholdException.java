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
 * Thrown when a request tries to create an owner whose lastName and address already
 * belong to another owner (compared case-insensitively with collapsed whitespace) and
 * the request has not opted in via {@code sharesHousehold}.
 * <p>
 * Signals a conflict with existing data so the {@link ExceptionControllerAdvice}
 * can surface it to the client as a {@code 409 Conflict} response.
 */
public class DuplicateHouseholdException extends RuntimeException {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with lastName '" + lastName + "' at address '" + address + "' already exists");
    }
}
