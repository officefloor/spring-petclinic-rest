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

import java.util.List;

/**
 * Thrown when a request carries one or more fields whose values are present but invalid.
 * <p>
 * Carries the names of the offending fields so the exception handler can report them
 * to the client in the response body's {@code errors} array.
 */
public class InvalidFieldsException extends RuntimeException {

    private final List<String> invalidFields;

    public InvalidFieldsException(List<String> invalidFields) {
        super("The request contains invalid values for fields: " + invalidFields);
        this.invalidFields = List.copyOf(invalidFields);
    }

    public List<String> getInvalidFields() {
        return this.invalidFields;
    }
}
