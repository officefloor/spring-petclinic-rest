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
 * Thrown when a supplied telephone value cannot be normalized to a valid E.164 number
 * (for example it has too few or too many digits). Handled by
 * {@link ExceptionControllerAdvice} as a 400 Bad Request.
 */
public class InvalidTelephoneException extends RuntimeException {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}
