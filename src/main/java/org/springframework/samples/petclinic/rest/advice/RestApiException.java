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
 * Base type for business-rule rejections raised by REST controllers. Each carries the HTTP
 * status the request must be rejected with and a human-readable detail; the shared exception
 * handling turns it into an RFC7807 {@code application/problem+json} response, so controllers
 * only have to signal <em>what</em> was rejected, not how the error body is shaped.
 */
public abstract class RestApiException extends RuntimeException {

    private final HttpStatus status;

    protected RestApiException(HttpStatus status, String detail) {
        super(detail);
        this.status = status;
    }

    /**
     * @return the HTTP status this rejection must be reported with
     */
    public HttpStatus getStatus() {
        return status;
    }
}
