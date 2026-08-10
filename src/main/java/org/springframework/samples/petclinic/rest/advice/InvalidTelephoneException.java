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
 * Thrown on create when the supplied telephone, after every non-digit character is stripped,
 * is not exactly 10 digits. Carries the {@code telephone} field name so the
 * {@link ExceptionControllerAdvice} can report it in the {@code errors} array of a 400 Bad
 * Request response, mirroring {@link RequiredFieldsMissingException}.
 */
public class InvalidTelephoneException extends RuntimeException {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}
