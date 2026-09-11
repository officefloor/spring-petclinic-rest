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
 * Base type for the create-time rules that reject a new owner with a 409 Conflict. Each concrete
 * subclass stands for one rule (e.g. a duplicate telephone or a city at capacity) and supplies the
 * fixed, client-facing {@code detail} that describes the conflict, while its {@code message} carries
 * the contextual text used for logging. All subclasses are handled uniformly by
 * {@link ExceptionControllerAdvice} as a 409 Conflict, so a new conflict rule only needs a new
 * subclass, not a new handler.
 */
public abstract class OwnerConflictException extends RuntimeException {

    private final String detail;

    protected OwnerConflictException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    /**
     * The fixed, client-facing description of the conflict, used as the {@code detail} of the
     * 409 Conflict {@code ProblemDetail} response.
     *
     * @return the client-facing conflict detail
     */
    public String getDetail() {
        return detail;
    }
}
