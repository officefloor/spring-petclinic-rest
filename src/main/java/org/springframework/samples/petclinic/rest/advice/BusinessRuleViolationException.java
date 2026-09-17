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

import org.springframework.http.HttpStatus;

/**
 * Signals that a request was rejected by a business rule (as opposed to a framework-level
 * validation failure). Carries the HTTP status to return together with the short {@code title}
 * and human-readable {@code detail} that populate the RFC 7807 {@code application/problem+json}
 * response produced by {@link ExceptionControllerAdvice}.
 */
public class BusinessRuleViolationException extends RuntimeException {

    private final HttpStatus status;

    private final String title;

    /**
     * @param status the HTTP status the rejection maps to (e.g. 400, 409, 429)
     * @param title  a short, human-readable summary of the problem type
     * @param detail a human-readable explanation specific to this occurrence
     */
    public BusinessRuleViolationException(HttpStatus status, String title, String detail) {
        super(detail);
        this.status = status;
        this.title = title;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public String getTitle() {
        return this.title;
    }
}
