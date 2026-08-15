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

/**
 * Thrown when an owner is created whose last name and address already match another owner (they
 * share a household), unless the request opts in with {@code sharesHousehold} set to {@code true}.
 * Handled as a 409 Conflict by {@link ExceptionControllerAdvice}.
 */
public class DuplicateHouseholdException extends RuntimeException {

    private final transient Object rejectedValue;

    public DuplicateHouseholdException(String lastName, String address) {
        super("an owner with the same last name and address already exists");
        this.rejectedValue = lastName + " / " + address;
    }

    public Object getRejectedValue() {
        return this.rejectedValue;
    }
}
