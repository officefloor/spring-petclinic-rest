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
 * Signals that a request has been rejected by a business rule with a specific client-error status
 * (typically {@code 400 BAD_REQUEST}, {@code 409 CONFLICT} or {@code 429 TOO_MANY_REQUESTS}).
 * <p>
 * Throwing this from a controller lets a rejection flow through {@link ExceptionControllerAdvice}
 * exactly like every other error the API reports, rather than each call site assembling its own
 * response. The {@link #getStatus() status} carries the HTTP status to report and the exception
 * message ({@link #getMessage()}) carries the human-readable reason for the rejection.
 */
public class RequestRejectedException extends RuntimeException {

    private final HttpStatus status;

    /**
     * @param status the HTTP status to report for this rejection
     * @param detail the human-readable reason the request was rejected
     */
    public RequestRejectedException(HttpStatus status, String detail) {
        super(detail);
        this.status = status;
    }

    /**
     * @return the HTTP status to report for this rejection
     */
    public HttpStatus getStatus() {
        return this.status;
    }

}
