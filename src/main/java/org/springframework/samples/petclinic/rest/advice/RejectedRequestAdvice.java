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

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Renders business-rule rejections (e.g. 409 Conflict, 429 Too Many Requests) as an
 * RFC 7807 {@code application/problem+json} body carrying {@code type}, {@code title},
 * {@code status} and {@code detail}. Controllers only need to throw a
 * {@link RejectedRequestException}; the chosen status is preserved unchanged.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RejectedRequestAdvice {

    @ExceptionHandler(RejectedRequestException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleRejection(RejectedRequestException e, HttpServletRequest request) {
        HttpStatus status = e.getStatus();
        ProblemDetail detail = ProblemDetail.forStatus(status);
        detail.setType(URI.create(request.getRequestURL().toString()));
        detail.setTitle(status.getReasonPhrase());
        detail.setDetail(e.getMessage());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Signals that a request was rejected by a business rule, pairing the HTTP status to
     * return with a human-readable detail for the problem+json body.
     */
    public static class RejectedRequestException extends RuntimeException {

        private final HttpStatus status;

        public RejectedRequestException(HttpStatus status, String detail) {
            super(detail);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return this.status;
        }
    }
}
