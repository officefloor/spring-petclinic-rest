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
 * Base type for the owner-request rules that reject a request and carry a fixed, client-facing
 * {@code detail} describing the problem, distinct from the {@code message} which carries the
 * contextual text used for logging. Its subtypes {@link OwnerConflictException} (a 409 Conflict)
 * and {@link InvalidOwnerFieldException} (a 400 Bad Request) fix the HTTP status their concrete
 * rules map to, so a new rule only needs a new subclass, not a new handler.
 */
public abstract class OwnerRuleException extends RuntimeException {

    private final String detail;

    protected OwnerRuleException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    /**
     * The fixed, client-facing description of the rule violation, used as the {@code detail} of the
     * {@code ProblemDetail} response.
     *
     * @return the client-facing detail
     */
    public String getDetail() {
        return detail;
    }
}
