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

import org.springframework.http.HttpStatus;

/**
 * Signals that a request the controller has begun to process must be rejected with a specific
 * client-error {@link HttpStatus} — {@code 400 Bad Request}, {@code 409 Conflict} or
 * {@code 429 Too Many Requests}. Request handling throws this instead of building a rejection
 * response itself, so the single translation from a rejection to its HTTP response lives in
 * {@link ExceptionControllerAdvice#handleRejectedRequestException}, alongside the other
 * exception-to-response mappings.
 */
public class RejectedRequestException extends RuntimeException {

    private final HttpStatus status;

    public RejectedRequestException(HttpStatus status) {
        this.status = status;
    }

    /**
     * The client-error status the rejected request must fail with.
     */
    public HttpStatus getStatus() {
        return this.status;
    }
}
