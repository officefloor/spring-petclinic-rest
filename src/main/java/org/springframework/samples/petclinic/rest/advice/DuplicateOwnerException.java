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

import java.util.List;

/**
 * Base type for the "an owner just like this one already exists" conflicts raised while creating an
 * owner. Each subtype identifies a single uniqueness rule (telephone, household, ...) and carries the
 * two pieces of data the {@link ExceptionControllerAdvice} needs to render a uniform
 * {@code 409 Conflict}: a client-facing {@linkplain #getDetail() detail} message and the
 * {@linkplain #getErrorFields() names of the offending request fields}. Handling all of them through
 * this base keeps the advice to one handler, so a new uniqueness rule adds a subtype rather than
 * another near-identical handler.
 */
public abstract class DuplicateOwnerException extends RuntimeException {

    private final String detail;

    private final List<String> errorFields;

    /**
     * @param message     the internal (log/diagnostic) message, typically naming the offending value
     * @param detail      the client-facing detail reported in the {@code 409 Conflict} response body
     * @param errorFields the names of the request fields that collided, reported in the response
     *                    {@code errors} array
     */
    protected DuplicateOwnerException(String message, String detail, List<String> errorFields) {
        super(message);
        this.detail = detail;
        this.errorFields = List.copyOf(errorFields);
    }

    public String getDetail() {
        return this.detail;
    }

    public List<String> getErrorFields() {
        return this.errorFields;
    }
}
