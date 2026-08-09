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
 * Signals that a request could not be completed because it conflicts with the current state of
 * the resource, resulting in a {@code 409 Conflict}. Handled by {@link ExceptionControllerAdvice}
 * which renders it as an RFC 7807 {@code application/problem+json} response.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String detail) {
        super(detail);
    }

}
