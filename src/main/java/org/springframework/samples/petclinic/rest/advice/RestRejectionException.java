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
 * Signals that a request was rejected by a business rule and must be reported to the client with a
 * specific {@link HttpStatus} (a 400, 409 or 429). It is handled centrally by
 * {@link ExceptionControllerAdvice}, which renders every rejection as an RFC&nbsp;7807
 * {@code application/problem+json} body carrying {@code type}, {@code title}, {@code status} and
 * {@code detail}. Controllers throw this instead of returning a bodyless {@code ResponseEntity} so
 * that all rejection responses share one problem-detail representation.
 */
public class RestRejectionException extends RuntimeException {

    private final HttpStatus status;

    public RestRejectionException(HttpStatus status, String detail) {
        super(detail);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return this.status;
    }
}
