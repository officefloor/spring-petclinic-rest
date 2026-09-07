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
 * Thrown when a request supplies one or more fields whose value is present but
 * fails a business rule (for example a telephone that does not normalize to
 * exactly ten digits).
 * <p>
 * Carries the names of the offending fields so the {@link ExceptionControllerAdvice}
 * can surface them to the client under the {@code errors} property of a
 * {@code 400 Bad Request} response.
 */
public class InvalidFieldsException extends RuntimeException {

    private final List<String> fields;

    public InvalidFieldsException(List<String> fields) {
        super("Invalid field values: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
