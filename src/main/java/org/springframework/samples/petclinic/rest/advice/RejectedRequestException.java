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
 * Signals that a request was rejected by a business rule and must fail with a specific
 * client-error status (e.g. {@code 400 Bad Request}, {@code 409 Conflict} or
 * {@code 429 Too Many Requests}). Thrown from controller handling in place of returning a
 * bare-status {@link org.springframework.http.ResponseEntity}, so every rejection is turned
 * into a response in one place — {@link ExceptionControllerAdvice#handleRejectedRequestException}
 * — rather than being built inline at each rejection site.
 *
 * @author Alexander Dudkin
 */
public class RejectedRequestException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Create a rejection carrying the HTTP status the request should fail with.
     *
     * @param status the client-error status to report for this rejection
     */
    public RejectedRequestException(HttpStatus status) {
        this.status = status;
    }

    /**
     * The HTTP status this rejection should be reported with.
     *
     * @return the rejection status
     */
    public HttpStatus getStatus() {
        return this.status;
    }

}
