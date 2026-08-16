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

import java.util.List;

/**
 * Thrown when a request supplies a field whose value is present but fails a business rule (for
 * example a telephone number that is not exactly ten digits after non-digit characters are
 * stripped). Carries the name of each offending field so the exception handler can report them in
 * the {@code errors} response body.
 */
public class InvalidFieldValueException extends RuntimeException {

    private final List<String> fields;

    public InvalidFieldValueException(String field, String message) {
        super(message);
        this.fields = List.of(field);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
