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
 * Raised when an owner submitted to the create endpoint has one or more required fields that are
 * present but blank (whitespace-only), which Bean Validation's {@code @Size(min = 1)} constraint
 * does not catch. Carries the names of the offending fields so the {@code ExceptionControllerAdvice}
 * can report them in the {@code errors} array of a 400 response.
 */
public class InvalidOwnerFieldsException extends RuntimeException {

    private final List<String> fields;

    public InvalidOwnerFieldsException(List<String> fields) {
        super("Blank owner fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    /**
     * @return the names of the required fields that were blank
     */
    public List<String> getFields() {
        return this.fields;
    }
}
