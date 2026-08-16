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
 * Thrown when a new owner shares the same household as an existing owner (the household being
 * keyed on the normalized last name and the postcode) and the request did not opt in via the
 * {@code sharesHousehold} flag. {@link ExceptionControllerAdvice} reports it back to the client
 * as a 409 Conflict response.
 */
public class DuplicateHouseholdException extends RuntimeException {

    public DuplicateHouseholdException(String message) {
        super(message);
    }

}
