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
 * Signals that a request was rejected by a business rule with a specific 4xx status
 * (typically {@code 400}, {@code 409} or {@code 429}). Thrown from REST controllers in
 * place of returning a bare status so that {@link ExceptionControllerAdvice} can render a
 * uniform RFC 7807 {@code application/problem+json} body for every rejection.
 *
 * <p>The exception message is used as the problem's {@code detail} member.
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
