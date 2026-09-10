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
 * Base type for the field-level validation failures that carry the names of the offending
 * fields. A subclass is thrown for a specific kind of failure (a missing required field, a
 * present-but-invalid value); the {@link ExceptionControllerAdvice} renders every one of them
 * the same way, surfacing the field names under the {@code errors} property of a
 * {@code 400 Bad Request} response.
 */
public abstract class FieldValidationException extends RuntimeException {

    private final List<String> fields;

    protected FieldValidationException(String message, List<String> fields) {
        super(message);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
