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
 * Thrown when a request to create or update an owner supplies a field whose value is present but
 * malformed (for example a telephone that does not reduce to exactly ten digits). Carries the names
 * of the offending fields so the {@link ExceptionControllerAdvice} can report them back to the
 * client as a 400 Bad Request.
 */
public class InvalidOwnerFieldsException extends RuntimeException {

    private final List<String> invalidFields;

    public InvalidOwnerFieldsException(List<String> invalidFields) {
        super("Invalid owner fields: " + invalidFields);
        this.invalidFields = List.copyOf(invalidFields);
    }

    public List<String> getInvalidFields() {
        return this.invalidFields;
    }

}
